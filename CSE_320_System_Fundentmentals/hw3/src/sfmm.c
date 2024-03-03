/**
 * All functions you make for the assignment must be implemented in this file.
 * Do not submit your assignment with a main function in this file.
 * If you submit with a main function in this file, you will get a zero.
 */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../include/debug.h"
#include "../include/sfmm.h"
#include <errno.h>

static sf_block* split_unallocated_block(size_t size_of_the_block_you_need, sf_block *block_to_split);
static void insert_block(struct sf_block *block);
static sf_block* construct_empty_block(void* start_address, size_t block_size);
static size_t figure_out_minimum_block_size(size_t size);
static int figure_out_minimum_block_class(sf_header block_header_or_block_size);
static void* get_free_block_from_lists(size_t size_you_need);
static int increase_heap(void);
static void collace_block(sf_block *block);
static sf_block* realloc_split(size_t size_of_the_block_you_need, sf_block* block_to_split);

extern struct sf_block sf_free_list_heads[NUM_FREE_LISTS];

// creates an empty block at a given address of a given size.
// it is assumed that the block starts at the footer of the previous block and the previous footer is already set
// Its body link is not set. This will done by insert
static sf_block* construct_empty_block(void* start_address, size_t block_size){
    sf_block *block =  (sf_block*)(start_address);
    block->header = (block_size);
    // check if the total block size is multiple of 16, if not, then return null
    if((block->header % 16) != 0){
        printf("\nsize of the block is not multiple of 16\n");
        return NULL;
    }
    block->header = block->header | 0b001;
    sf_footer *block_footer = (sf_footer*) (((void*)block) + (block->header & BLOCK_SIZE_MASK)); // at the pre_field of the next block struct, 8 bytes before epilogue if the last block in heap
    *block_footer = block->header ^ sf_magic(); // this address is the footer and will be the prev_footer field of the next
    return block;
}


// splits a larger block into a minimum block needed, return the minimum block and store the left over into the freelists
// this is assuming you already figure out the minimum block size. Size of the smaller split block is NOT the payload but the whole block
static sf_block* split_unallocated_block(size_t size_of_the_block_you_need, sf_block *block_to_split){
    size_t block_to_split_size = block_to_split->header & BLOCK_SIZE_MASK;
    if((block_to_split_size - size_of_the_block_you_need) < 32){
        return block_to_split; // if the splitted block will result in a block less than the size of 32 then don't split
    }

    // take the block t split out of the linked list. We are gonna reinsert it based on the new size
    block_to_split->body.links.prev->body.links.next = block_to_split->body.links.next;
    block_to_split->body.links.next->body.links.prev = block_to_split->body.links.prev;

    // next split the block
    sf_header last_two_bits = block_to_split->header & 0b011;
    sf_block* block_you_need = block_to_split;
    // prev_header field of the new block can stay the same, the header field needs to be adjusted
    block_you_need->header = 0; // blank out the header
    block_you_need->header = size_of_the_block_you_need | last_two_bits;
    sf_block* remianing_block = (sf_block*) ((void*)block_you_need + (block_you_need->header & BLOCK_SIZE_MASK));
    remianing_block->prev_footer = block_you_need->header ^ sf_magic();
    remianing_block->header = (block_to_split_size - size_of_the_block_you_need) | 0b001;
    //remianing_block->header = remianing_block->header | 0b001; // commented out because not working too well with show blocks
    //remianing_block->header & BLOCK_SIZE_MASK; // this is to zero out the last tow bits, commented out because the compiler complains
    sf_footer* reamining_block_footer = (sf_footer*) ((void*) remianing_block + (remianing_block->header & BLOCK_SIZE_MASK));
    *reamining_block_footer = remianing_block->header ^ sf_magic(); // resets the prev_footer field of the next block struct

    insert_block(block_you_need);
    insert_block(remianing_block);
    return block_you_need;
}

// like the split unallocated blocks but for allocated blocks, used by realloc
static sf_block* realloc_split(size_t size_of_the_block_you_need, sf_block* block_to_split){
    size_t block_to_split_size = block_to_split->header & BLOCK_SIZE_MASK;
    if((block_to_split_size - size_of_the_block_you_need) < 32){
        return block_to_split; // if the splitted block will result in a block less than the size of 32 then don't split
    }

    // next split the block
    sf_header last_two_bits = block_to_split->header & 0b011;
    sf_block* block_you_need = block_to_split;
    // prev_header field of the new block can stay the same, the header field needs to be adjusted
    block_you_need->header = 0; // blank out the header
    block_you_need->header = size_of_the_block_you_need | last_two_bits;
    sf_block* remianing_block = (sf_block*) ((void*)block_you_need + (block_you_need->header & BLOCK_SIZE_MASK));
    remianing_block->prev_footer = block_you_need->header ^ sf_magic();
    remianing_block->header = (block_to_split_size - size_of_the_block_you_need);
    remianing_block->header = remianing_block->header | 0b001;
    //remianing_block->header & BLOCK_SIZE_MASK; // this is to zero out the last tow bits, commented out because the compiler complains
    sf_footer* reamining_block_footer = (sf_footer*) ((void*) remianing_block + (remianing_block->header & sf_magic()));
    *reamining_block_footer = remianing_block->header ^ sf_magic(); // resets the prev_footer field of the next block struct
    sf_header* next_block_header = reamining_block_footer + 1;
    //turn the prev_allocate bit of the next block header off
    *next_block_header = *next_block_header & 0xfffffffffffffffe;
    // update the footer of the next block
    sf_footer* next_block_footer = (sf_footer*) ((void*)reamining_block_footer + (*next_block_header & BLOCK_SIZE_MASK));
    *next_block_footer = *next_block_header ^ sf_magic();

    insert_block(remianing_block);
    collace_block(remianing_block);
    return block_you_need;
}

// self explanatory, increase heap by one page, relocate the epilogue, create a empty block out of it, coelace with adjacent blocks, then insert it to the free lists
static int increase_heap(void){
    sf_epilogue *old_epiloque = (sf_epilogue*) (sf_mem_end() - sizeof(sf_epilogue));

    void* new_break;
    // if we can't grow then we are fucked.
    if((new_break = sf_mem_grow()) == NULL){
        return -1;
    }

    sf_epilogue * new_epilogue = (sf_epilogue*) (sf_mem_end() - sizeof(sf_epilogue));

    // copy the content of the old epilogue to the new epilogue
    memcpy((void*)new_epilogue, (void*)old_epiloque, sizeof(struct sf_epilogue));

    void* ptr = (void*) (((void*) old_epiloque) - sizeof(sf_footer)); // get the prev_footer field of the block before the old prologue, this will be the starting address of our new block struct
    size_t payload_size = PAGE_SZ - sizeof(sf_epilogue) - sizeof(sf_footer); // Because 8 bytes are taken from the page size to create new epilogue and the new footer field for the new block
    size_t block_size = figure_out_minimum_block_size(payload_size);
    sf_block* new_block = construct_empty_block(ptr, block_size);
    // inserts the block
    insert_block(new_block);


    // we combined the new_block with the block immediately before it if it is empty. This ensures if the user request insanely large size, it can be found if total amount of free space is available
    collace_block(new_block);
    return 0;
}

// combines the block with an empty block in front and back
static void collace_block(sf_block *block){
    sf_header *back_header = (sf_header*) ((void*)block + (block->header & BLOCK_SIZE_MASK)); // at prev_footer field of the next block struct
    sf_header *front_header = (sf_header*) (((void*) block) - ((block->prev_footer ^ sf_magic()) & BLOCK_SIZE_MASK)); // at the prev_footer field of the previous block struct
    back_header += 1; // at the proper header field of the next block struct
    front_header += 1; // at the proper header field of the previous block struct

    // combines the back if none allocated and none epilogue, block will absorb the next block
    if((*back_header & 0b010) == 0){
        sf_header original_last_two_bits = block->header & 0b011;
        sf_block *back_block = (sf_block*) (back_header - 1);
        back_block->body.links.prev->body.links.next = back_block->body.links.next;
        back_block->body.links.next->body.links.prev = back_block->body.links.prev;
        block->header = (block->header & BLOCK_SIZE_MASK) + (back_block->header & BLOCK_SIZE_MASK);
        memset((void*) &back_block->prev_footer, 0, sizeof(sf_footer)); // blanked out the absorbed block prev_footer struct field just in case
        memset((void*) &back_block->header, 0, sizeof(sf_header)); // blanked out the absorbed block header struct field just in case
        block->header = block->header | original_last_two_bits;
        sf_footer *footer =(sf_footer *) ((void*) block + (block->header & BLOCK_SIZE_MASK));
        *footer = block->header ^ sf_magic(); // reset the footer field of this new block

        // need to severed and reinsert the enlarge block in case it needs a bigger class size
        block->body.links.prev->body.links.next = block->body.links.next;
        block->body.links.next->body.links.prev = block->body.links.prev;
        insert_block(block);
    }


    // combines the front if none allocated and none prologue, front block will absorb the block
    if((*front_header & 0b010) == 0){
        sf_block *front_block = (sf_block*) (front_header - 1);
        sf_header original_last_two_bits = front_block->header & 0b011;
        block->body.links.prev->body.links.next = block->body.links.next;
        block->body.links.next->body.links.prev = block->body.links.prev;
        front_block->header = (front_block->header & BLOCK_SIZE_MASK) + (block->header & BLOCK_SIZE_MASK);
        memset((void*) &block->prev_footer, 0, sizeof(sf_footer)); // blanked out the absorbed block prev_footer struct field just in case
        memset((void*) &block->header, 0, sizeof(sf_header)); // blanked out the absorbed block header struct field just in case
        front_block->header = front_block->header | original_last_two_bits;
        sf_footer *footer =(sf_footer *) ((void*) front_block + (front_block->header & BLOCK_SIZE_MASK));
        *footer = front_block->header ^ sf_magic(); // reset the footer field of this new block

        // need to severed and reinsert the enlarge block in case it needs a bigger class size
        front_block->body.links.prev->body.links.next = front_block->body.links.next;
        front_block->body.links.next->body.links.prev = front_block->body.links.prev;
        insert_block(front_block);
    }


    return;
}

// get a free block from a free list, split it if too large
static void* get_free_block_from_lists(size_t size_you_need){
    size_t minimum_block_size = figure_out_minimum_block_size(size_you_need);
    int block_class = figure_out_minimum_block_class(minimum_block_size);
    sf_block* first_fit_block = NULL;

    SEARCH_AGAIN: // label is better than doing recursion

    for(int loop_class = block_class; loop_class < 9; loop_class += 1){
        sf_block* sentinel = &sf_free_list_heads[loop_class];
        sf_block* next = sentinel->body.links.next;

        if((next == sentinel) || next == NULL)
            continue;

        if((next->header & BLOCK_SIZE_MASK) >= minimum_block_size){
            first_fit_block = next;
            break;
        }

        while((next = next->body.links.next) != sentinel){
            if(next->header >= minimum_block_size){
                first_fit_block = next;
                goto BREAK_OUT_OF_LOOPS;
            }
        }
    }

    BREAK_OUT_OF_LOOPS:
    if(first_fit_block == NULL){

        if(increase_heap() == -1)
            return NULL;

        goto SEARCH_AGAIN;
    }

    // if block is too large then split it
    if((first_fit_block->header & BLOCK_SIZE_MASK) != size_you_need){
        first_fit_block = split_unallocated_block(minimum_block_size, first_fit_block);
    }
    // take the first fit block out of the linked list
    first_fit_block->header = first_fit_block->header | 0b010; // turn the allocated bits on
    sf_header previous_header = first_fit_block->prev_footer ^ sf_magic();

    if(previous_header & 0b010)
        first_fit_block->header = first_fit_block->header | 0b001; // if previous block is allocated, turn the last bit on

    first_fit_block->body.links.prev->body.links.next = first_fit_block->body.links.next;
    first_fit_block->body.links.next->body.links.prev = first_fit_block->body.links.prev;
    sf_footer* first_fit_block_footer =  (sf_footer*)((void*)(first_fit_block) + (first_fit_block->header & BLOCK_SIZE_MASK));
    *first_fit_block_footer = first_fit_block->header ^ sf_magic();



    // return the address of the payload
    return (void*) &first_fit_block->body.payload;
}

// insert a new empty block into its minimum class list
static void insert_block(struct sf_block *block){
    int block_class = figure_out_minimum_block_class(block->header);
    struct sf_block *sentinel = &sf_free_list_heads[block_class];

    // if the list is empty
    if(((sentinel->body.links.next == sentinel) && (sentinel->body.links.prev == sentinel)) || (sentinel->body.links.next == NULL || sentinel->body.links.prev == NULL)){
        sentinel->body.links.next = block;
        sentinel->body.links.prev = block;
        block->body.links.prev = sentinel;
        block->body.links.next = sentinel;
    }else{
        block->body.links.next = sentinel->body.links.next;
        sentinel->body.links.next->body.links.prev = block;

        sentinel->body.links.next = block;
        block->body.links.prev = sentinel;
    }
}

// find the minimum block class for a given block size passed as block header
static int figure_out_minimum_block_class(sf_header block_header_or_block_size){
    int size_class = 0;
    size_t block_size = (block_header_or_block_size & BLOCK_SIZE_MASK);
    for(size_t size = 32; block_size > size; size = size << 1){
        size_class += 1;
        if(size_class == 8) break;
    }

    return size_class;
}

// figure out the minimum size of a block given the size of its payload
static size_t figure_out_minimum_block_size(size_t payload_size){
    size_t minimum_size = payload_size + sizeof(sf_header) + sizeof(sf_footer);
    while(((minimum_size % 16) != 0) || minimum_size < 32){
        minimum_size += 1;
    }
    return minimum_size;
}

// this is to be run the first time when mallocating
static int set_up_prolog_epilog(void){
    void *new_break = sf_mem_grow();

    if(new_break == NULL ){
        return -1;
    }

    sf_prologue* start = (sf_prologue*)new_break;
    sf_epilogue* end = (sf_epilogue*) (sf_mem_end() - sizeof(sf_epilogue));
    start->header = (sizeof(*start) - 8) | 0x03;
    start->footer = start->header ^ sf_magic();
    end->header = 0x0 | 0x02;
    size_t payload_size = PAGE_SZ - (sizeof(*start) + sizeof(*end) + sizeof(sf_header) + sizeof(sf_footer)); // blocks previous footer will share the same memory address as the prologue footer
    sf_block *block;
    void* ptr = (void* )(start + 1);
    ptr -= 8; // align the starting block struct prev_footer to the same address as the prologue footer
    if((block = construct_empty_block(ptr, figure_out_minimum_block_size(payload_size))) == NULL){
        printf("\nconstruct empty block error\n");
        return -1;
    }

    insert_block(block);
    return 0;
}

void *sf_malloc(size_t size) {

    if((void*)size == NULL){
        return NULL;
    }

    if(sf_mem_start() == sf_mem_end()){

        for(int i = 0; i < 10; i++){
            sf_free_list_heads[i].body.links.prev = &sf_free_list_heads[i];
            sf_free_list_heads[i].body.links.next = &sf_free_list_heads[i];
        }

        if(set_up_prolog_epilog() == -1){
            sf_errno = ENOMEM;
            return NULL;
        }

    }


    void* retval;
    if((retval = get_free_block_from_lists(size)) == NULL){
        sf_errno = ENOMEM;
        return NULL;
    }



    return retval;
}

void sf_free(void *pp) {
    sf_header* pp_header = (sf_header*)(pp - sizeof(sf_header));
    size_t block_size = *pp_header & BLOCK_SIZE_MASK;
    sf_footer* pp_footer = (sf_footer*)(pp + (block_size - sizeof(sf_header) - sizeof(sf_footer)));
    sf_footer* pp_prev_footer = (sf_footer*) (pp_header-1);
    sf_header prev_header = *pp_prev_footer ^ sf_magic();
    sf_header* actual_previous_header = (sf_header*) ( ((void*)pp_header) - (prev_header & BLOCK_SIZE_MASK));
    sf_block* free_block = (sf_block*) pp_prev_footer;



    // if null then abort
    if(pp == NULL){
        abort();
    }

    // if header is not allocated then abort
    if((*pp_header & 0b010) == 0){
        abort();
    }


    // if block size is less than 32, abort
    if(block_size < 32){
        abort();
    }

    void* epilogue_start = sf_mem_end() - sizeof(sf_epilogue);
    // if the footer of the ptr is at or after the epilogue then abort
    if((void*)pp_footer >= epilogue_start){
        abort();
    }


    void* prologue_end = sf_mem_start() + sizeof(sf_prologue);
    // if ptr header is before the prologue then abort
    if((void*)pp_header < prologue_end){
        abort();
    }

    // if the previous header ^ with magic does not equal the prev_footer of this block struct field
    if((*actual_previous_header ^ sf_magic()) != *pp_prev_footer){
        abort();
    }

    // if the allocted bit of prev_footer of this block does not equal the allocated bit of the actual header of the previous block, then abort
    if((*actual_previous_header & 0b010) != (prev_header & 0b010)){
        abort();
    }

    // now free when all checks are ready
    sf_header last_bit = free_block->header & 0b001;
    free_block->header = free_block->header & BLOCK_SIZE_MASK;
    free_block->header = free_block->header | last_bit;
    insert_block(free_block);
    collace_block(free_block);
    *pp_footer = free_block->header ^ sf_magic();
    sf_header* next_block_header = pp_footer + 1; // now at the header of the next block
    // turn off the prev_allocated in the header of the next block
    *next_block_header = *next_block_header & 0xfffffffffffffffe;
    // update the footer of the next block
    sf_footer* next_block_footer = (sf_footer*) ((void*)pp_footer + (*next_block_header & BLOCK_SIZE_MASK));
    *next_block_footer = *next_block_header ^ sf_magic();
    return;
}

void *sf_realloc(void *pp, size_t rsize) {

    sf_block* original_block = (sf_block*) (pp - 2 * sizeof(size_t));
    size_t original_payload_size = (original_block->header & BLOCK_SIZE_MASK) - 2 * sizeof(size_t);
    char temporay_storage[original_payload_size];
    memcpy(temporay_storage, pp, original_payload_size);

    if(rsize == 0){
        sf_free(pp);
        return NULL;
    }

    if(rsize > original_payload_size){
        sf_free(pp);
        void* new_payload_address = sf_malloc(rsize);
        memcpy(new_payload_address, temporay_storage, original_payload_size);
        return new_payload_address;
    }

    if(rsize < original_payload_size){
        size_t minimum_block_size = figure_out_minimum_block_size(rsize);
        sf_block* new_block = realloc_split(minimum_block_size, original_block);
        return &new_block->body.payload;
    }

    if(rsize == (size_t)original_block){
        return pp;
    }

    return NULL;
}

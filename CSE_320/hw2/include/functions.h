//
// Created by xgao on 10/3/19.
//

#ifndef HW2_FUNCTIONS_H
#define HW2_FUNCTIONS_H

#include "datadef.h"
int rolo_delete (Ptr_Rolo_List link);
int cathelpfile (char* filepath, char* helptopic, int clear);
int user_eof();
int any_char_to_continue();
int clear_the_screen();
void display_entry (Ptr_Rolo_Entry entry);
void display_entry_for_update ( Ptr_Rolo_Entry entry);
int rolo_menu_yes_no (char* prompt, int rtn_default, int help_allowed, char* helpfile, char* subject);
int rolo_insert (Ptr_Rolo_List link , int (*compare)());
void save_and_exit (int rval);
int rolo_menu_data_help_or_abort (char* prompt, char* helpfile, char* subject, char** ptr_response);
void display_field_names();
int rolo_menu_number_help_or_abort (char* prompt, int low, int high,int* ptr_ival);
int entry_action (Ptr_Rolo_List rlink);
void display_list_of_entries (Ptr_Rolo_List rlist);
void summarize_entry_list ( Ptr_Rolo_List rlist ,char *ss);
int rolo_main (int argc, char *argv[]);
void interactive_rolo ();
void print_people();
int print_short ();
int rolo_reorder ();
int rlength (Ptr_Rolo_List rlist);
int read_rolodex (int fd);
int clearinit ();
void write_rolo_list (FILE *fp);
void write_rolo ( FILE *fp1,FILE *fp2);
int rolo_peruse_mode (Ptr_Rolo_List first_rlink);
int rolo_update_mode (Ptr_Rolo_List rlink);
void roloexit (int rval);
void rolo_add ();
int select_field_to_search_by (int *ptr_index,char **ptr_name);
int rolo_search_mode (int field_index,char* field_name, char* search_string);
void save_to_disk ();
#endif //HW2_FUNCTIONS_H

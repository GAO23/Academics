import React from "react";
import Modal from "react-bootstrap/Modal";
import {SERVER_URL} from "../../common/Constants";
import ProfileButtons from "./ProfileButtons";



class FindSimilarApplicantsModal extends React.Component{

    constructor(props) {
        super(props);
        this.state = {
            current_page_num: 1
        }
        this.get_profiles = this.get_profiles.bind(this)
        this.get_page_buttons = this.get_page_buttons.bind(this);
        this.is_whole_number = this.is_whole_number.bind(this);
        this.button_list = [];
    }



    get_profiles(){
      let profiles = this.props.current_modal_collage_profiles;
      if(profiles.length === 0) return (<h1>No similar students found</h1>);
      let result = profiles.map((element) =>{
          return(<ProfileButtons key={element.username} data={element}/>);
      });
      return result;
    }

    is_whole_number(n) {
        let result = (n - Math.floor(n)) !== 0;

        if (result)
            return false;
        else
            return true;
    }

    get_page_buttons(){
        let page_list = [];
        let new_button_list = [];
        let max_pages = this.props.current_modal_collage_profiles.length / 10;
        if(!this.is_whole_number(max_pages)) {
            max_pages = Math.floor(max_pages) + 1;
        }
        let next_ten_pages = this.state.current_page_num + 9;
        let old_beginning = this.button_list[0];
        let old_end = this.button_list[this.button_list.length - 1];
        if(this.props.current_modal_collage_profiles.length <= 10) {
            max_pages = 1;
        }

        if(this.state.current_page_num === 1){
            for(let i = 1 ; i <= max_pages && i <= next_ten_pages; i++){
                new_button_list.push(i);
                page_list.push(
                    <li  key={`key-${i}`} className="page-item">
                        <button onClick={this.page_clicked} className={`page-link shadow-none ${(i === this.state.current_page_num) ? "active" : ""}`}>{i}</button>
                    </li>
                )
            }
        }else if(this.state.current_page_num === old_end && !(this.button_list.length < 10) && (this.state.current_page_num !== max_pages)){
            for(let i = old_end; i <= max_pages && i <= next_ten_pages; i++){
                new_button_list.push(i);
                page_list.push(
                    <li  key={`key-${i}`} className="page-item">
                        <button onClick={this.page_clicked} className={`page-link shadow-none ${(i === this.state.current_page_num) ? "active" : ""}`}>{i}</button>
                    </li>
                )
            }
        }else if(this.state.current_page_num === old_beginning){
            let previous_ten = this.state.current_page_num - 9;
            if(previous_ten < 1) previous_ten = 1;
            for(let i = previous_ten ; i <= max_pages && i <= old_beginning; i++){
                new_button_list.push(i);
                page_list.push(
                    <li  key={`key-${i}`} className="page-item">
                        <button onClick={this.page_clicked} className={`page-link shadow-none ${(i === this.state.current_page_num) ? "active" : ""}`}>{i}</button>
                    </li>
                )
            }
        }else if((this.button_list.includes(this.state.current_page_num)) || (this.button_list.length < 10)){
            for(let i = old_beginning ; i <= old_end; i++){
                new_button_list.push(i);
                page_list.push(
                    <li  key={`key-${i}`} className="page-item">
                        <button onClick={this.page_clicked} className={`page-link shadow-none ${(i === this.state.current_page_num) ? "active" : ""}`}>{i}</button>
                    </li>
                )
            }
        }
        if(this.props.current_modal_collage_profiles.length === 0){
            this.button_list = [];
            return [];
        }
        this.button_list = new_button_list;
        return page_list;
    }


    render() {
      let profiles = this.get_profiles();
      let buttons = this.get_page_buttons();
        return (
            <Modal show={this.props.show} onHide={this.props.show_collage_modal} aria-labelledby="plot-modal-label" centered>
                <div className="modal-content">
                    <Modal.Header closeButton>
                        <Modal.Title>Similar applicants</Modal.Title>
                    </Modal.Header>
                    <div className="modal-body">
                        {/* Initially, there should be no tags inside the tag below. */}
                        <div className="similar-applicant-list">
                          {/* .similar-applicant-name tag and .similar-applicant-info tag are a pair */}
                          {profiles}
                        </div>
                      </div>
                    </div>
                     <nav>
                        {/* Initially, there should be no tags inside the tag below. */}
                        <ul className="pagination" id="pagination">
                            {/* "active" class below means the current active page button  */}
                            {/* first page button must be active in default after completing search */}
                            {
                                buttons
                            }
                        </ul>
                     </nav>
            </Modal>
        )
    }


}


export default FindSimilarApplicantsModal;
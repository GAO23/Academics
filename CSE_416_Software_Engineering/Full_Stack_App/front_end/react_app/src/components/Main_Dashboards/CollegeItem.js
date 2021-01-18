import React from "react";
import {Link} from "react-router-dom";
import SearchCollege from "./SearchCollege";


class CollegeItem extends React.Component{

    constructor(props) {
        super(props);
        this.state = {
            display : {display: "None"},
            modal_clicked: false
        }

        this.show_college_details = this.show_college_details.bind(this);
    }

    show_college_details(event){
        if(this.state.modal_clicked){
            this.state.modal_clicked = false;
            return;
        }
        this.setState({display: {display: (this.state.display.display === "None") ? "flex" : "None"}});
    }

    render() {
        let {recommendation, college_id, state, name, institution, admission_rate, tuition, debt, completion, ranking, size} = this.props.data;
        let current_sort = this.props.current_sort;
        return(
            <React.Fragment>
                <div className="list-group-item list-group-item-action" style={{zIndex:"0"}} onClick ={this.show_college_details}>
                    <h5 className="college-name">{name}</h5>
                        <button style={{display: (current_sort === SearchCollege.sort_enum.RECOMMENDATION) ? "" : "None"}} className="find-similar-applicants" data-toggle="modal" data-target="#find-similar-applicants-modal"
                            onClick={()=>{
                                this.props.show_collage_modal();
                                this.state.modal_clicked = true;
                                this.props.set_current_modal_college(name);
                            }}
                    >
                        Find Similar Applicants
                    </button>
                    <h5>{state}</h5>
                </div>
                <div className="item-info" style={this.state.display}>
                    <table>
                        <tbody>
                        <tr>
                            <td><b>{institution}</b><br/><label>Institution</label></td>
                            <td><b>{admission_rate + "%"}</b><br/><label>Admission rate</label></td>
                            <td><b>{completion +"%"}</b><br/><label>Completion rate</label></td>
                            <td><b style={{color: "white"}}>{recommendation}</b><br/><label style={{color: "white"}}>Recommendation Score</label></td>
                        </tr>
                        <tr>
                            <td><b>{`$${tuition}`}</b><br/><label>Cost of attendance</label></td>
                            <td><b>{"$" + debt}</b><br/><label>Median debt amount</label></td>
                            <td><b>{ranking}</b><br/><label>Ranking</label></td>
                            <td><b>{size}</b><br/><label>Size</label></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </React.Fragment>
        )
    }
}

export default CollegeItem;

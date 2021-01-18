import React from "react";

class ProfileButtons extends React.Component{
    constructor(props) {
        super(props);
        this.state = {
            display: "None"
        }
        this.clicked= this.clicked.bind(this);
        this.sanitized_list = this.sanitized_list.bind(this);
        this.page_clicked = this.page_clicked.bind(this);
    }

    page_clicked(event){
       let button = event.target;
       let number = button.innerHTML;
       this.setState({current_page_num: parseInt(number)})
    }


    clicked(event){
        let new_display = (this.state.display === "None") ? "flex" : "None";
         this.setState({display: new_display});
    }

    sanitized_list(){
        for (let property in this.props.data){
            this.props.data[property] = (this.props.data[property] == null) ? "-" : this.props.data[property];
        }
        let keys = ["application_status", "username", "residence_state", "high_school_name", "high_school_city", "high_school_state", "gpa", "college_class",
                    "major_1", "major_2", "sat_math", "sat_ebrw", "act_english", "act_math", "act_reading", "act_science", "act_composite", "sat_lit", "sat_us",
                    "sat_world", "sat_math_1", "sat_math_2", "sat_eco_bio", "sat_mol_bio", "sat_chem", "sat_physics", "ap_passed"];
        keys.forEach((element) => {
            let field = this.props.data[element];
            if(typeof field === 'undefined'){
                this.props.data[element] = "-";
            }
        })
    }

    render() {
        this.sanitized_list();
        let {application_status, username, residence_state, high_school_name, high_school_city, high_school_state, gpa, college_class } = this.props.data;
        let {major_1, major_2, sat_math, sat_ebrw, act_english, act_math, act_reading, act_science, act_composite, sat_lit, sat_us, sat_world, sat_math_1, sat_math_2, sat_eco_bio, sat_mol_bio, sat_chem, sat_physics, ap_passed} = this.props.data;

        return (
            <React.Fragment>
                <button onClick={this.clicked} className="list-group-item list-group-item-action similar-applicant-name">{username}</button>
                  <div style={{display: this.state.display}} className="similar-applicant-info item-info">
                    <ul>
                      <li>{`College class: ${college_class}`}</li>
                      <li>{`Residence state: ${residence_state}`}</li>
                      <li>{`High School: ${high_school_name}, ${high_school_city}, ${high_school_state}`}</li>
                      <li> {`Major 1: ${major_1}`}</li>
                      <li>{`Major 2: ${major_2}`}</li>
                      <li>{`APs Passed: ${ap_passed}`}</li>
                      <li>{`GPA: ${3.8}`}</li>
                      <li>{`SAT Math: ${sat_math}`}</li>
                      <li>{`SAT EBRW: ${sat_ebrw}`}</li>
                      <li>{`ACT Composite: ${act_composite}`}</li>
                      <li>{`ACT English: ${act_english}`}</li>
                      <li>{`ACT Math: ${act_math}`}</li>
                      <li>{`ACT Reading: ${act_reading}`}</li>
                      <li>{`ACT Science: ${act_science}`}</li>
                      <li>{`SAT2 Chemistry: ${sat_chem}`}</li>
                      <li>{`SAT2 Eco-Bio: ${sat_eco_bio}`}</li>
                      <li>{`SAT2 Literature: ${sat_lit}`}</li>
                      <li>{`SAT2 Math I: ${sat_math_1}`}</li>
                      <li>{`SAT2 Math II: ${sat_math_2}`}</li>
                      <li>{`SAT2 Mol-Bio: ${sat_mol_bio}`}</li>
                      <li>{`SAT2 Physics: ${sat_physics}`}</li>
                      <li>{`SAT2 US History: ${sat_us}`}</li>
                      <li>{`SAT2 World History: ${sat_world}`}</li>
                    </ul>
                  </div>
            </React.Fragment>
        )
    }
}

export default ProfileButtons;
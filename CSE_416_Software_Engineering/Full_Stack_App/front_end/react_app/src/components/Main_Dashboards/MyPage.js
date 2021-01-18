import React from "react";
import '../../gui/css/my_page.css';
import RejectedImg from '../../gui/img/rejected.png';
import StudentImg from '../../gui/img/student.png';
import ConfirmImg from '../../gui/img/confirmed.png';
import QuestionImg from '../../gui/img/question.png';
import FindSimilarHighSchoolsModal from "./FindSimilarHighSchoolsModal";
import {SERVER_URL, NOT_AUTHENTICATED_ERROR, EXPIRED_MSG} from "../../common/Constants";
import SearchCollege from "./SearchCollege";
import Authenticator from "../../common/Authenticator";

const SIMILAR_HIGH_SCHOOLS_ENDPOINT = "/find_similar_highschools";
const GET_PROFILE_ENDPOINT = "/get_profile";
const SAVE_PROFILE_ENDPOINT = "/save_profile";
const MAJOR_ENDPOINT = "/all_majors";
const ADMISSION_DECISION_ENDPOINT = "/get_admission_decision";
const RECOMMENDED_COLLEGE_ENDPOINT = "/get_college_list";
const SUBMIT_COLLEGE_DECISION = "/submit_admission_decision";

class MyPage extends React.Component{


    constructor(props) {
        super(props);
        this.state={
            show_high_school_modal: false,
            current_modal_high_schools: [],
            profile: {
                username: "-",
                residence_state: "-",
                high_school_name: "",
                high_school_city: "",
                high_school_state: "-",
                gpa: "",
                college_class: "",
                major_1: "-",
                major_2: "-",
                sat_math: "",
                sat_ebrw: "",
                act_english: "",
                act_math: "",
                act_reading: "",
                act_science: "",
                act_composite: "",
                sat_lit: "",
                sat_us: "",
                sat_world: "",
                sat_math_1: "",
                sat_math_2: "",
                sat_eco_bio: "",
                sat_mol_bio: "",
                sat_chem: "",
                sat_physics: "",
                ap_passed: '',
                password: ""
            },
            admission_input_data: {
                college_name: "-",
                status: "-"
            },
            majors_list: [],
            admission_decisions: [],
            current_page_num: 1,
            mounted: false,
            colleges: [],
            find_high_school_disable: false,
            unedited_user_high_school: "-"
        }
        this.button_list = [];
        this.show_high_school_modal = this.show_high_school_modal.bind(this);
        this.fetch_similar_high_schools = this.fetch_similar_high_schools.bind(this);
        this.fetch_profile = this.fetch_profile.bind(this);
        this.set_exam_scores = this.set_exam_scores.bind(this);
        this.post_save = this.post_save.bind(this);
        this.get_profile_body = this.get_profile_body.bind(this);
        this.fetch_majors = this.fetch_majors.bind(this);
        this.get_majors = this.get_majors.bind(this);
        this.fetch_admissions = this.fetch_admissions.bind(this);
        this.get_page_buttons = this.get_page_buttons.bind(this);
        this.is_whole_number = this.is_whole_number.bind(this);
        this.get_admission_decisions = this.get_admission_decisions.bind(this);
        this.get_admission_colleges = this.get_admission_colleges.bind(this);
        this.filter_acceptances = this.filter_acceptances.bind(this);
        this.fetch_colleges = this.fetch_colleges.bind(this);
        this.on_admission_submit = this.on_admission_submit.bind(this);
        this.page_clicked = this.page_clicked.bind(this);
    }

    page_clicked(event){
       let button = event.target;
       let number = button.innerHTML;
       this.setState({current_page_num: parseInt(number)})
    }


    get_admission_decisions(){
        if(this.state.admission_decisions.length === 0){
            return null;
        }

        let list = [];
        let beginning = (this.state.current_page_num === 1) ? 0 : (this.state.current_page_num - 1) * 10;
        let end_index = beginning + 10;

        for(let i = beginning; (i < this.state.admission_decisions.length) && (i < end_index); i++){
            let admission = this.state.admission_decisions[i];
            let image = null;
            let toolbox = "";
            if(admission.verification === "Approved"){
                image = ConfirmImg;
                toolbox = "Decision successfully processed";
            }else if(admission.verification === "Pending"){
                image = QuestionImg;
                toolbox = "Decision under review";
            }else{
                image = RejectedImg;
                toolbox = "Decision marked as questionable";
            }
            list.push(
               <tr key={admission.college}>
                    <td className="wrap-admin-check">
                    <img src={image}
                         className="admin-check"
                         data-toggle="tooltip" data-placement="top"
                         title={toolbox}
                    />
                    </td>
                    <td className="college">{admission.college}</td>
                    <td className="status">{admission.status}</td>
                </tr>
            )
        }
        return list;
    }

    show_high_school_modal(){
        this.fetch_similar_high_schools().then(() =>{
             this.setState({show_high_school_modal: !this.state.show_high_school_modal});
        }).catch((err)=>{
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            alert(err.message);
        });
    }

    async fetch_similar_high_schools(){
        try{
            let response = await fetch(
                SERVER_URL + SIMILAR_HIGH_SCHOOLS_ENDPOINT,
                {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json'
                    }
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            this.state.current_modal_high_schools = response_json.data;
        }catch (err) {
            console.log(err.stack);
            throw new Error(err.message);
        }
    }

    async fetch_profile(){
        try{
            let response = await fetch(
                SERVER_URL + GET_PROFILE_ENDPOINT,
                {
                    method: "POST",
                    credentials: 'include',
                    headers:{
                        "Accept": "application/json"
                    }
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            for (let key in response_json.profile) {
                response_json.profile[key] = (response_json.profile[key] == null) ? "" : response_json.profile[key];
            }
            response_json.profile.username = response_json.username;
            this.setState({unedited_user_high_school:response_json.profile.high_school_name, mounted: true, profile: {...this.state.profile, ...response_json.profile}});
        }catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            console.log(err.stack);
            alert(err.message);
        }
    }

    async fetch_admissions(){
        try{
            let response = await fetch(
                SERVER_URL + ADMISSION_DECISION_ENDPOINT,
                {
                    method: "POST",
                    credentials: "include",
                    headers:{
                         'Accept': 'application/json'
                    }
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            this.state.admission_decisions = response_json.admission_decisions;
        }catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            console.log(err.stack);
            alert(err.message);
        }
    }
    
    async componentDidMount() {
        await this.fetch_majors();
        await this.fetch_admissions();
        await this.fetch_colleges();
        await this.fetch_profile();
    }

    async fetch_majors(){
        try{
            let response = await fetch(SERVER_URL + MAJOR_ENDPOINT);
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            this.state.majors_list = response_json.majors;
        } catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            console.log(err.stack);
            alert(err.message);
        }
    }

    get_majors(){
        let list = [];
        this.state.majors_list.forEach(element=>{
            list.push(
                <option key={element} value={element}>{element}</option>
            );
        });
        return list;
    }

    set_exam_scores(score_key, new_score){
        let new_profile = {...this.state.profile};
        new_profile[score_key] = new_score
        this.setState({profile: new_profile});
    }

    get_profile_body(){
        let body = {};
        for(let key in this.state.profile){
            if(this.state.profile[key] === "" || this.state.profile[key] === "-"){
                body[key] = null;
                continue;
            }

            else if (key.includes("sat")) {
                if(this.state.profile[key] < 200 || this.state.profile[key] > 800) throw new Error("All sat scores should be 200-800");
                body[key] = parseInt(this.state.profile[key]);
            }

            else if(key.includes("act")){
                if(this.state.profile[key] < 1 || this.state.profile[key] > 36) throw new Error("All act scores should be 1-36");
                body[key] = parseInt(this.state.profile[key]);
            }

            else if(key.includes("gpa")){
                 if(this.state.profile[key] < 0 || this.state.profile[key] > 4.0) throw new Error("GPA should be 0-4.0");
                 body[key] = parseFloat(this.state.profile[key]);
            }

            else if(key.includes("password")){
                let regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/g;
                if(!this.state.profile[key].match(regex)) throw new Error('Password not meet the requirement');
                body[key] = this.state.profile[key];
            }

            else if(key.includes("class")){
                if(this.state.profile[key] > 2027 || this.state.profile[key] < 0) throw new Error("College class should be <= 2027")
                 body[key] = parseInt(this.state.profile[key]);
            }

            else if(key.includes("ap")){
                if(this.state.profile[key] < 0) throw new Error("AP can not be negative");
                body[key] = parseInt(this.state.profile[key]);
            }

            else{
                body[key] = this.state.profile[key];
            }


        }
        return body;
    }

    async post_save(){
        try{
            let body = this.get_profile_body();
            if(body.password == null) body.password = undefined;
            console.log(body);
            let response = await fetch(
                SERVER_URL + SAVE_PROFILE_ENDPOINT,
                {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(body)
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            alert("success!");
            window.location.reload(false);
        }catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
           console.log(err.stack);
           alert(`Failed to save: ${err.message}`);
        }
    }

    get_page_buttons(){
        let page_list = [];
        let new_button_list = [];
        let max_pages = this.state.admission_decisions.length / 10;
        if(!this.is_whole_number(max_pages)) {
            max_pages = Math.floor(max_pages) + 1;
        }
        let next_ten_pages = this.state.current_page_num + 9;
        let old_beginning = this.button_list[0];
        let old_end = this.button_list[this.button_list.length - 1];
        if(this.state.admission_decisions.length <= 10) {
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
        if(this.state.admission_decisions.length === 0){
            this.button_list = [];
            return [];
        }
        this.button_list = new_button_list;
        return page_list;
    }

    is_whole_number(n) {
        let result = (n - Math.floor(n)) !== 0;

        if (result)
            return false;
        else
            return true;
    }

    async fetch_colleges(){
        try{
            let filter_data = {
                name: null,
                admission_rate: {min: null, max: null},
                location: null,
                sat_ebrw: {min: null, max: null},
                max_ranking: null,
                size: null,
                sat_math: {min: null, max: null},
                major: {left: null, right: null},
                max_tuition: null,
                act: {min: null, max: null},
                policy: "strict",
                sort: SearchCollege.sort_enum.NAME
            };
            let response = await fetch(
                SERVER_URL + RECOMMENDED_COLLEGE_ENDPOINT,
                {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(filter_data)
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            let result = response_json.colleges.map((element)=>{
                return element.name;
            });
            this.state.colleges = result;
        }catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            console.log(err.stack);
            alert(err.message);
        }
    }

    get_admission_colleges(){
        let colleges = this.state.colleges.map((element)=>{
             return(
                <option key={element} value={element}>{element}</option>
             );
        });

        return colleges;
    }

    filter_acceptances(){
        let acceptances = this.state.admission_decisions;
        if(this.admission_decisions.college_name !== "-"){
            acceptances = acceptances.filter((element)=>{
               return(element.college === this.admission_decisions.college_name)
            });
        }

        if(this.admission_decisions.status !== "-"){
            acceptances = acceptances.filter((element)=>{
               return(element.status.toLowerCase().includes(this.admission_decisions.status.toLowerCase()));
            });
        }
        return acceptances;
    }

    async on_admission_submit(event){
        try{
            let body = this.state.admission_input_data;
            if(body.college_name === "-") throw new Error("choose a college");
            if(body.status === "-") throw new Error("choose a status");
            console.log(body);
            let response = await fetch(
                SERVER_URL + SUBMIT_COLLEGE_DECISION,
                {
                    method: "POST",
                    credentials : "include",
                    headers:{
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(body)
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            await this.fetch_admissions();
            this.forceUpdate();
        }catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            console.log(err.stack);
            alert(err.message);
        }
    }

    render() {
        let majors = this.get_majors();
        let admission_colleges = this.get_admission_colleges();
        let acceptances = this.get_admission_decisions();
        let page_buttons = this.get_page_buttons();
        if(!this.state.mounted)return null;
        return (
            <React.Fragment>
                <div className="right-content overflow-hidden">
                    <div className="wrap-my-page">
                        <div id="profile">
                            <div>
                                <span className="result-text">Profile</span>
                                <button
                                    type="button"
                                    id="save-btn"
                                    className="btn btn-primary shadow-none"
                                    onClick={async (event)=>{
                                        await this.post_save();
                                    }}
                                >Save
                                </button>
                            </div>
                            <div>
                                {/* put student's data as a value of input tags on the first rendering */}
                                <table className="PageTable" style={{width: '100%'}}>
                                    <tbody>
                                    <tr>
                                        <td rowSpan={2} className="wrap-student-icon"><img className="student-icon"
                                                                                           src={StudentImg}/></td>
                                        {/* put student's username inside b tag below */}
                                        <td className="profile-username"><h5><b>{this.state.profile.username}</b></h5></td>
                                    </tr>
                                    <tr>
                                        <td className="flex"><b>Class of</b><input type="number" placeholder="<= 2027"
                                                                                   className="form-control shadow-none profile-number-input"
                                                                                   value={this.state.profile.college_class}
                                                                                   onChange={(event)=>{this.setState({profile: {...this.state.profile, college_class: event.target.value}})}}
                                                                                   id="profile-college-class"
                                                                                   min="0"
                                                                                   max={2027}/></td>
                                    </tr>
                                    <tr>
                                        <td><b>Password</b></td>
                                        <td><input type="password" className="form-control shadow-none"
                                                   id="profile-password" minLength={8} maxLength={128}
                                                   value={this.state.profile.password}
                                                   onChange={(event)=>{
                                                       this.setState({profile: {...this.state.profile, password: event.target.value}})
                                                   }}
                                        /></td>
                                    </tr>
                                    <tr style={{marginBottom: '10px'}}>
                                        <td><b>Residence state</b></td>
                                        <td>
                                            <select id="profile-residence-state"
                                                    value={this.state.profile.residence_state}
                                                    onChange={event => {this.setState({profile: {...this.state.profile, residence_state: event.target.value}})}}
                                            >
                                                {/* - means no choice */}
                                                <option value="-">-</option>
                                                <option value="AL">AL</option>
                                                <option value="AK">AK</option>
                                                <option value="AZ">AZ</option>
                                                <option value="AR">AR</option>
                                                <option value="CA">CA</option>
                                                <option value="CO">CO</option>
                                                <option value="CT">CT</option>
                                                <option value="DE">DE</option>
                                                <option value="FL">FL</option>
                                                <option value="GA">GA</option>
                                                <option value="HI">HI</option>
                                                <option value="ID">ID</option>
                                                <option value="IL">IL</option>
                                                <option value="IN">IN</option>
                                                <option value="IA">IA</option>
                                                <option value="KS">KS</option>
                                                <option value="KY">KY</option>
                                                <option value="LA">LA</option>
                                                <option value="ME">ME</option>
                                                <option value="MD">MD</option>
                                                <option value="MA">MA</option>
                                                <option value="MI">MI</option>
                                                <option value="MN">MN</option>
                                                <option value="MS">MS</option>
                                                <option value="MO">MO</option>
                                                <option value="MT">MT</option>
                                                <option value="NE">NE</option>
                                                <option value="NV">NV</option>
                                                <option value="NH">NH</option>
                                                <option value="NJ">NJ</option>
                                                <option value="NM">NM</option>
                                                <option value="NY">NY</option>
                                                <option value="NC">NC</option>
                                                <option value="ND">ND</option>
                                                <option value="OH">OH</option>
                                                <option value="OK">OK</option>
                                                <option value="OR">OR</option>
                                                <option value="PA">PA</option>
                                                <option value="RI">RI</option>
                                                <option value="SC">SC</option>
                                                <option value="SD">SD</option>
                                                <option value="TN">TN</option>
                                                <option value="TX">TX</option>
                                                <option value="UT">UT</option>
                                                <option value="VT">VT</option>
                                                <option value="VA">VA</option>
                                                <option value="WA">WA</option>
                                                <option value="WV">WV</option>
                                                <option value="WI">WI</option>
                                                <option value="WY">WY</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td rowSpan={3} style={{verticalAlign: 'baseline'}}><b>High school</b></td>
                                        <td><input type="text" className="form-control shadow-none"
                                                   id="profile-high-scool-name" placeholder="Name" minLength={11}
                                                   value={this.state.profile.high_school_name}
                                                   onChange={(event)=>{
                                                        this.state.find_high_school_disable = true;
                                                       this.setState({profile: {...this.state.profile, high_school_name: event.target.value}})
                                                   }}
                                        /></td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <input type="text" className="form-control shadow-none"
                                                   id="profile-high-scool-city" placeholder="City" minLength={5}
                                                    value={this.state.profile.high_school_city}
                                                   onChange={(event)=>{
                                                       this.setState({profile: {...this.state.profile, high_school_city: event.target.value}})
                                                   }}
                                            />
                                            <select
                                                id="profile-high-school-state"
                                                value={this.state.profile.high_school_state}
                                                onChange={(event)=>{
                                                     this.setState({profile: {...this.state.profile, high_school_state: event.target.value}})
                                                }}
                                            >
                                                {/* - means no choice */}
                                                <option value="-">-</option>
                                                <option value="AL">AL</option>
                                                <option value="AK">AK</option>
                                                <option value="AZ">AZ</option>
                                                <option value="AR">AR</option>
                                                <option value="CA">CA</option>
                                                <option value="CO">CO</option>
                                                <option value="CT">CT</option>
                                                <option value="DE">DE</option>
                                                <option value="FL">FL</option>
                                                <option value="GA">GA</option>
                                                <option value="HI">HI</option>
                                                <option value="ID">ID</option>
                                                <option value="IL">IL</option>
                                                <option value="IN">IN</option>
                                                <option value="IA">IA</option>
                                                <option value="KS">KS</option>
                                                <option value="KY">KY</option>
                                                <option value="LA">LA</option>
                                                <option value="ME">ME</option>
                                                <option value="MD">MD</option>
                                                <option value="MA">MA</option>
                                                <option value="MI">MI</option>
                                                <option value="MN">MN</option>
                                                <option value="MS">MS</option>
                                                <option value="MO">MO</option>
                                                <option value="MT">MT</option>
                                                <option value="NE">NE</option>
                                                <option value="NV">NV</option>
                                                <option value="NH">NH</option>
                                                <option value="NJ">NJ</option>
                                                <option value="NM">NM</option>
                                                <option value="NY">NY</option>
                                                <option value="NC">NC</option>
                                                <option value="ND">ND</option>
                                                <option value="OH">OH</option>
                                                <option value="OK">OK</option>
                                                <option value="OR">OR</option>
                                                <option value="PA">PA</option>
                                                <option value="RI">RI</option>
                                                <option value="SC">SC</option>
                                                <option value="SD">SD</option>
                                                <option value="TN">TN</option>
                                                <option value="TX">TX</option>
                                                <option value="UT">UT</option>
                                                <option value="VT">VT</option>
                                                <option value="VA">VA</option>
                                                <option value="WA">WA</option>
                                                <option value="WV">WV</option>
                                                <option value="WI">WI</option>
                                                <option value="WY">WY</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <button type="button" id="find-similar-high-schools-btn"
                                                    className="btn btn-danger shadow-none" data-toggle="modal"
                                                    data-target="#find-similar-high-schools-modal"
                                                    disabled={this.state.find_high_school_disable}
                                                    onClick={this.show_high_school_modal}
                                            >Find similar high schools
                                            </button>
                                            <FindSimilarHighSchoolsModal user_high_school={this.state.unedited_user_high_school} show={this.state.show_high_school_modal} current_modal_high_schools={this.state.current_modal_high_schools} show_high_school_modal={this.show_high_school_modal}/>                                        </td>
                                    </tr>
                                    <tr>
                                        <td><b>Major 1</b></td>
                                        <td>
                                            <select
                                                id="profile-major1"
                                                value={this.state.profile.major_1}
                                                onChange={(event)=>{
                                                    this.setState({profile: {...this.state.profile, major_1: event.target.value}})
                                                }}
                                            >
                                                {/* - means no choice */}
                                                <option value="-">-</option>
                                                {
                                                    majors
                                                }
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><b>Major 2</b></td>
                                        <td>
                                            <select
                                                id="profile-major1"
                                                value={this.state.profile.major_2}
                                                onChange={(event)=>{
                                                    this.setState({profile: {...this.state.profile, major_2: event.target.value}})
                                                }}
                                            >
                                                {/* - means no choice */}
                                                <option value="-">-</option>
                                                {
                                                     majors
                                                }
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><b>AP passed</b></td>
                                        <td>
                                            <input type="number"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-ap-passed" min={0}
                                                   value={this.state.profile.ap_passed}
                                                   onChange={(event)=>{
                                                       this.setState({profile: {...this.state.profile, ap_passed: event.target.value}})
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><b>GPA</b></td>
                                        <td>
                                            <input type="number" placeholder="<= 4.0"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-gpa" max={4} step="0.01"
                                                    value={this.state.profile.gpa}
                                                   onChange={(event)=>{
                                                       this.setState({profile: {...this.state.profile, gpa: event.target.value}})
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                                <table className="PageTable">
                                    <tbody>
                                    <tr>
                                        <td><b>Exam scores</b></td>
                                    </tr>
                                    <tr>
                                        <td>SAT Math</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat-math" min={200} max={800}
                                                   value={this.state.profile.sat_math}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_math", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>SAT EBRW</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat-ebrw" min={200} max={800}
                                                   value={this.state.profile.sat_ebrw}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_ebrw", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>ACT Composite</td>
                                        <td>
                                            <input type="number" placeholder="1 - 36"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-act-composite" min={1} max={36}
                                                   value={this.state.profile.act_composite}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("act_composite", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>ACT English</td>
                                        <td>
                                            <input type="number" placeholder="1 - 36"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-act-english" min={1} max={36}
                                                   value={this.state.profile.act_english}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("act_english", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>ACT Math</td>
                                        <td>
                                            <input type="number" placeholder="1 - 36"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-act-math" min={1} max={36}
                                                   value={this.state.profile.act_math}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("act_math", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>ACT Reading</td>
                                        <td>
                                            <input type="number" placeholder="1 - 36"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-act-reading" min={1} max={36}
                                                   value={this.state.profile.act_reading}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("act_reading", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>ACT Science</td>
                                        <td>
                                            <input type="number" placeholder="1 - 36"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-act-science" min={1} max={36}
                                                   value={this.state.profile.act_science}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("act_science", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>SAT2 Chemistry</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-chemistry" min={200} max={800}
                                                   value={this.state.profile.sat_chem}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_chem", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>SAT2 Eco-Bio</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-eco-bio" min={200} max={800}
                                                   value={this.state.profile.sat_eco_bio}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_eco_bio", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>SAT2 Literature</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-literature" min={200} max={800}
                                                   value={this.state.profile.sat_lit}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_lit", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>SAT2 Math I</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-math1" min={200} max={800}
                                                   value={this.state.profile.sat_math_1}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_math_1", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>SAT2 Math II</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-math2" min={200} max={800}
                                                   value={this.state.profile.sat_math_2}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_math_2", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>SAT2 Mol-Bio</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-mol-bio" min={200} max={800}
                                                   value={this.state.profile.sat_mol_bio}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_mol_bio", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>SAT2 Physics</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-physics" min={200} max={800}
                                                   value={this.state.profile.sat_physics}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_physics", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>SAT2 US History</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-us-history" min={200} max={800}
                                                   value={this.state.profile.us_history}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("us_history", event.target.value);
                                                   }}
                                            />
                                        </td>
                                        <td>SAT2 World History</td>
                                        <td>
                                            <input type="number" placeholder="200 - 800"
                                                   className="form-control shadow-none profile-number-input"
                                                   id="profile-sat2-world-history" min={200} max={800}
                                                   value={this.state.profile.sat_world}
                                                   onChange={(event)=>{
                                                       this.set_exam_scores("sat_world", event.target.value);
                                                   }}
                                            />
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div id="application">
                            <div>
                                <span className="result-text">Admission Decisions</span>
                            </div>
                            <div>
                                <table className="PageTable">
                                    <tbody>
                                    <tr>
                                        <td><b>College</b></td>
                                        <td>
                                            <select
                                                onChange={(event)=>{
                                                    this.setState({admission_input_data: {...this.state.admission_input_data, college_name: event.target.value}})
                                                }}
                                                value={this.state.admission_input_data.college_name}
                                                id="college"
                                                required
                                            >
                                                {/* - means no choice */}
                                                <option value="-">-</option>
                                                {
                                                    admission_colleges
                                                }
                                            </select>
                                        </td>
                                        <td rowSpan={2}>
                                            <button type="button" id="register-btn"
                                                    className="btn btn-primary shadow-none"
                                                    onClick={this.on_admission_submit}
                                            >Submit
                                            </button>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><b>Status</b></td>
                                        <td>
                                            <select
                                                onChange={(event)=>{
                                                    this.setState({admission_input_data: {...this.state.admission_input_data, status: event.target.value}})
                                                }}
                                                value={this.state.admission_input_data.status}
                                                id="status"
                                                required
                                            >
                                                {/* - means no choice */}
                                                <option value="-">-</option>
                                                <option value="Accepted">Accepted</option>
                                                <option value="Denied">Denied</option>
                                                <option value="Deferred">Deferred</option>
                                                <option value="Wait-listed">Wait-listed</option>
                                                <option value="Withdrawn">Withdrawn</option>
                                                <option value="Pending">Pending</option>
                                            </select>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                            <div>
                                <table className="PageTable">
                                    <tbody>
                                    {acceptances}
                                    </tbody>
                                </table>
                            </div>
                            <nav>
                                {/* Initially, there should be no tags inside the tag below. */}
                                <ul className="pagination" id="pagination">
                                    {/* "active" class below means the current active page button  */}
                                    {/* first page button must be active in default after completing search */}
                                    {page_buttons}
                                </ul>
                            </nav>
                        </div>
                    </div>
                </div>
            </React.Fragment>

        );
    }
}

export default MyPage;
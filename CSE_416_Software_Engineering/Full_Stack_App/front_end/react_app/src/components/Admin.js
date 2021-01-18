import React from "react";
import "../gui/css/admin.css";
import LogoImg from "../gui/img/logo.png";
import LogoutImg from "../gui/img/logout.png";
import Authenticator from "../common/Authenticator";
import {Redirect} from "react-router-dom";
import {account_enum} from "../common/Authenticator";
import Reviews from "./Admin_Dashboards/Reviews";
import {SERVER_URL, NOT_AUTHENTICATED_ERROR, EXPIRED_MSG} from "../common/Constants";
import {AdminProvider} from "../common/GlobalContext";

const DELETE_ALL_STUDENT_ENDPOINT = "/delete_all_students";
const DECIDE_ENDPOINT =  "/decide_admission_decision";
const IMPORT_SCORE_CARD_ENDPOINT = "/import_college_scorecard";
const SCRAP_COLLEGE_DATA_ENDPOINT = "/update_all_college_data";
const UPDATE_RANKING_ENDPOINT = "/update_rankings";
const GET_QUESTIONABLE_ENDPOINT = "/get_questionable_decisions";
const IMPORT_STUDENT_PROFILE_ENDPOINT = "/import_student_profile_applications";

class Admin extends React.Component{
    static admin_tab_enum={
        SCRAPE_DATA: "Scrape Data",
        IMPORT_DATA: "Import Data",
        DELETE_ALL: "Delete all student profiles",
        REVIEW: "Review questionable acceptance decisions"
    }

    constructor(props) {
        super(props);
        this.state = {
            current_tab: Admin.admin_tab_enum.SCRAPE_DATA,
            decisions: [],
            // college_score_card_form_data: null,
            scrape_college_data_disable: false,
            questionables: [],
            import_data_score_card_disable: false,
            import_student_data_disable: false,
            questionable_decisions : {},
            current_page_num: 1
        }
        this.button_list = [];
        this.on_logout = this.on_logout.bind(this);
        this.get_reviews = this.get_reviews.bind(this);
        this.on_delete_all_click = this.on_delete_all_click.bind(this);
        this.post_questionable_decisions = this.post_questionable_decisions.bind(this);
        this.on_scrap_college_ranking = this.on_scrap_college_ranking.bind(this);
        this.on_scrap_college_data = this.on_scrap_college_data.bind(this);
        this.on_submit_score_card_file = this.on_submit_score_card_file.bind(this);
        this.on_import_college_score_card = this.on_import_college_score_card.bind(this);
        this.fetch_questionables = this.fetch_questionables.bind(this);
        this.on_student_profile_import = this.on_student_profile_import.bind(this);
        this.append_decision = this.append_decision.bind(this);
        this.remove_decision = this.remove_decision.bind(this);
        this.on_acceptable = this.on_acceptable.bind(this);
        this.on_still_questionable = this.on_still_questionable.bind(this);
        this.get_page_buttons = this.get_page_buttons.bind(this);
        this.page_clicked = this.page_clicked.bind(this);
    }

    async on_acceptable(event){
        try{
            let body = [];
            for(let key in this.state.questionable_decisions){
                let element = this.state.questionable_decisions[key];
                element.status = "Approved";
                body.push({...element});
            }
            console.log(body);
            if(body.length === 0) throw new Error("tick a questionable decision first");
            let response = await fetch(
                SERVER_URL + DECIDE_ENDPOINT,
                {
                    method: "POST",
                    credentials: "include",
                    headers:{
                        "Accept": "application/json",
                        "Content-Type" : "application/json"
                    },
                    body: JSON.stringify(body)
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            await this.fetch_questionables();
            this.state.questionable_decisions = {};
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

    async on_still_questionable(event){
         try{
            let body = [];
            for(let key in this.state.questionable_decisions){
                let element = this.state.questionable_decisions[key];
                element.status = "Denied";
                body.push({...element});
            }
            console.log(body);
            if(body.length === 0) throw new Error("tick a questionable decision first");
            let response = await fetch(
                SERVER_URL + DECIDE_ENDPOINT,
                {
                    method: "POST",
                    credentials: "include",
                    headers:{
                        "Accept": "application/json",
                        "Content-Type" : "application/json"
                    },
                    body: JSON.stringify(body)
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            await this.fetch_questionables();
            this.state.questionable_decisions = {};
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

    append_decision(decision){
        this.state.questionable_decisions[decision.key] = decision;
    }

    remove_decision(decision){
        delete this.state.questionable_decisions[decision.key];
    }


    async on_logout(){
        let username = Authenticator.getUserName();
        await Authenticator.logout();
        this.props.history.push({
                    pathname: '/login',
                    state: { username:username }
                });
    }

    async post_questionable_decisions(body){
        try{

            let response = await fetch(
                SERVER_URL + DECIDE_ENDPOINT,
                {
                    method: 'POST',
                    credentials: "include",
                    headers: {
                        "Accept" : "application/json",
                        "Content-Type" : "application/json"
                    },
                    body: JSON.stringify(body)
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(`Recieved ${response_json.result} for {${body.student_name}, ${body.college_name}, ${body.status}`);
            return null;
        }catch (err) {
            if(err.message === NOT_AUTHENTICATED_ERROR){
                alert(EXPIRED_MSG);
                Authenticator.expiredSession();
                this.props.history.push({
                    pathname: '/login'
                });
                return;
            }
            return err;
        }
    }


    async on_scrap_college_data(event){
        try{
            alert("this may take a couple minutes so please wait patiently until you see a success popup");
            this.setState({scrape_college_data_disable: true});
            let response = await fetch(
                SERVER_URL + SCRAP_COLLEGE_DATA_ENDPOINT,
                {
                    method: "GET",
                    credentials: "include"
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            alert("Success!")
            this.setState({scrape_college_data_disable: false});
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
            this.setState({scrape_college_data_disable: false});
        }
    }

    async on_scrap_college_ranking(event){
        try{
            let response = await fetch(
                SERVER_URL + UPDATE_RANKING_ENDPOINT,
                {
                    method: "GET",
                    credentials: "include"
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            alert("Success!")
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

    async fetch_questionables(event){
        try{
            let response = await fetch(SERVER_URL + GET_QUESTIONABLE_ENDPOINT, {credentials:"include"});
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            this.state.questionables = response_json.result;
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

    get_reviews(){
        if(this.state.questionables.length === 0) return null;
        let questionables = [];
        let beginning = (this.state.current_page_num === 1) ? 0 : (this.state.current_page_num - 1) * 10;
        let end_index = beginning + 10;

        for(let i = beginning; (i < this.state.questionables.length) && (i < end_index); i++){
            let element = this.state.questionables[i];
            let college = element.college;
            let student = element.student;
            let {act_composite_25, act_composite_75, admission_rate, avg_act_composite, avg_gpa, avg_sat_ebrw, avg_sat_math, city, completion_rate, in_cost, institution, median_debt,
                out_cost, ranking, region, salary, sat_ebrw_25, sat_ebrw_75, sat_math_25, sat_math_75, size, state
            } = college;
            let {act_composite, act_english, act_math, act_reading, act_science, ap_passed, college_class, major_1, major_2,
                    gpa, sat_chem, sat_ebrw, sat_eco_bio, sat_lit, sat_math, sat_math_1, sat_math_2, sat_mol_bio, sat_physics, sat_us, sat_world, username
            } = student;
            let student_name = student.username;
            let college_name = college.name;
            let high_school_name = student.high_school_name;
            let high_school_state = student.high_school_state;
            let residence_state = student.residence_state;
            let high_school_city = student.high_school_city;
            questionables.push(
                <Reviews
                    key={`${element.timestamp}_${student_name}_${college_name}`}
                    questionable_key={`${element.timestamp}_${student_name}_${college_name}`}
                    btn_info={{
                        username: (student_name == null) ? "-" : student_name,
                        acceptance: "Accepted",
                        high_school: (high_school_name == null) ? "-" : high_school_name,
                        high_school_location: (high_school_state == null || high_school_city == null) ? "-" : `${high_school_city}, ${high_school_state}`,
                        college_name: (college_name == null) ? "-" : college_name
                    }}
                    personal={{
                        state: (residence_state == null) ? "-" : residence_state,
                        math: (sat_math == null) ? "-" : sat_math,
                        ap: (ap_passed == null) ? "-" : ap_passed,
                        majors1: (major_1 == null) ? "-" : major_1,
                        class: (college_class == null) ? "-"  : college_class,
                        ebrw: (sat_ebrw == null) ? "-" : sat_ebrw,
                        gpa: (gpa == null) ? "-" : gpa,
                        majors2: (major_2 == null) ? "-" : major_2
                    }}
                    sat2={{
                        chemistry: (sat_chem == null) ? "-" : sat_chem,
                        eco_bio: (sat_eco_bio == null) ? "-" : sat_eco_bio,
                        literature: (sat_lit == null) ? "-" : sat_lit,
                        mol_bio: (sat_mol_bio == null) ? "-" : sat_mol_bio,
                        math_I: (sat_math_1 == null) ? "-" : sat_math_1,
                        math_II: (sat_math_2 == null) ? "-" : sat_math_2,
                        physics: (sat_physics == null) ? "-" : sat_physics,
                        us_history: (sat_us == null) ? "-" : sat_us,
                        world_history: (sat_world == null) ? "-" : sat_world
                    }}
                    act={{
                        english: (act_english == null) ? "-" : act_english,
                        math: (act_math == null) ? "-" : act_math,
                        reading: (act_reading == null) ? "-" : act_reading,
                        science: (act_science == null) ? "-" : act_science,
                        composite: (act_composite == null) ? "-" : act_composite
                    }}
                    timestamp={element.timestamp}
                />
            );
        }

        return questionables;
    }

    on_submit_score_card_file(event){
        let files = event.target.files;
        let reader = new FileReader();
        reader.readAsDataURL(files[0]);
        reader.onload = (e)=>{
            console.warn("file data ", e.target.result);
            let form_data = {file: e.target.result};
            this.state.college_score_card_form_data = form_data;
        }
    }

    page_clicked(event){
       let button = event.target;
       let number = button.innerHTML;
       this.setState({current_page_num: parseInt(number)})
    }

    async on_delete_all_click(){
        try{
            let response = await fetch(
                SERVER_URL + DELETE_ALL_STUDENT_ENDPOINT,
                {
                    method : "GET",
                    credentials: "include",
                    headers: {
                        'Accept': 'application/json'
                    }
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            alert("Success!");
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
            alert(err);
        }
    }

    // async on_import_college_score_card(event){
    //     try{
    //         if(this.state.college_score_card_form_data == null) throw new Error("Choose a score card to import")
    //         let response = await post(SERVER_URL + IMPORT_SCORE_CARD_ENDPOINT, this.state.college_score_card_form_data);
    //         if(response.status !== 200) throw new Error("Failed, server returned: " + response.result);
    //     }catch (err) {
    //         console.log(err.stack);
    //         alert(err.message);
    //     }
    // }

    async on_import_college_score_card(event){
        try{
            alert("this may take a couple minutes so please wait patiently until you see a success popup");
            this.setState({import_data_score_card_disable: true});
            let response = await fetch(
                SERVER_URL + IMPORT_SCORE_CARD_ENDPOINT,
                {
                    credentials: "include",
                    method: "POST",
                    headers:{
                        "Accept" : "application/json"
                    }
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.status);
            alert("success");
            this.setState({import_data_score_card_disable: false});
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
            this.setState({scrape_college_data_disable: false});
        }
    }

    async on_student_profile_import(event){
        try{
            alert("this may take a couple minutes so please wait patiently until you see a success popup");
             this.setState({import_student_data_disable: true});
            let response = await fetch(
                SERVER_URL + IMPORT_STUDENT_PROFILE_ENDPOINT,
                {
                    credentials: "include",
                    method: "GET",
                    headers:{
                        "Accept" : "application/json"
                    }
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            alert("success!");
            this.setState({import_student_data_disable: false});
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
        let max_pages = this.state.questionables.length / 10;
        if(!this.is_whole_number(max_pages)) {
            max_pages = Math.floor(max_pages) + 1;
        }
        let next_ten_pages = this.state.current_page_num + 9;
        let old_beginning = this.button_list[0];
        let old_end = this.button_list[this.button_list.length - 1];
        if(this.state.questionables.length <= 10) {
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
        if(this.state.questionables.length === 0){
            this.button_list = [];
            return [];
        }
        this.button_list = new_button_list;
        return page_list;
    }


    render() {
        let from = this.props.location;
        if(Authenticator.getAccountType() === account_enum.STUDENT){
            return (<Redirect to={
                        {
                            pathname: "/main",
                            state: {
                                from: from
                            }
                        }
                    }/>);
        }

        let reviews = this.get_reviews();
        let page_lists = this.get_page_buttons();
        return (
            <AdminProvider value={{append_decision: this.append_decision, remove_decision: this.remove_decision}}>
                <div className="wrap-dashboard">
                <div className="left-menu">
                    <div className="wrap-logo"><img className="logo" src={LogoImg} alt="logo"/></div>
                    <div>
                        {/* replace # with appropriate end point  */}
                        <button onClick={this.on_logout} >
                            <div className="wrap-icon"><img src={LogoutImg} alt="logout"/></div>
                            <div>Log out</div>
                        </button>
                    </div>
                </div>
                <div className="right-content">
                    <div className="nav-tabs">
                        <ul className="nav nav-pills">
                            <li className="nav-item">
                                <button
                                    className={`nav-link ${(this.state.current_tab === Admin.admin_tab_enum.SCRAPE_DATA) ? "active" : ""} scrape`}
                                    onClick={()=>{
                                        this.setState({current_tab: Admin.admin_tab_enum.SCRAPE_DATA});
                                    }}
                                >
                                    Scrape Data
                                </button>
                            </li>
                            <li className="nav-item">
                                <button
                                    className={`nav-link ${(this.state.current_tab === Admin.admin_tab_enum.IMPORT_DATA) ? "active" : ""} import`}
                                    onClick={()=>{
                                        this.setState({current_tab: Admin.admin_tab_enum.IMPORT_DATA});
                                    }}
                                >
                                    Import Data
                                </button>
                            </li>
                            <li className="nav-item">
                                <button
                                    className={`nav-link ${(this.state.current_tab === Admin.admin_tab_enum.DELETE_ALL) ? "active" : ""} delete`}
                                    onClick={()=>{
                                        this.setState({current_tab: Admin.admin_tab_enum.DELETE_ALL});
                                    }}
                                >
                                    Delete all student profiles
                                </button>
                            </li>
                            <li className="nav-item">
                                <button
                                    className={`nav-link ${(this.state.current_tab === Admin.admin_tab_enum.REVIEW) ? "active" : ""} review`}
                                    onClick={()=>{
                                        this.fetch_questionables().then(()=>{
                                             this.setState({current_tab: Admin.admin_tab_enum.REVIEW});
                                        });
                                    }}
                                >
                                    Review questionable acceptance decisions
                                </button>
                            </li>
                        </ul>
                    </div>
                    <div className="nav-content">
                        <div className="scrape" style={
                            {display: (this.state.current_tab === Admin.admin_tab_enum.SCRAPE_DATA) ?
                                "" : "None"
                            }}
                        >
                            <div>
                                <div>
                                    <h3>Scrape college rankings</h3>
                                    <button onClick={this.on_scrap_college_ranking} className="btn btn-primary">Scrape</button>
                                </div>
                                <span>Scrape WSJ/THE 2020 rankings of all colleges</span>
                            </div>
                            <div>
                                <div>
                                    <h3>Scrape CollegeData.com</h3>
                                    <button disabled={this.state.scrape_college_data_disable} onClick={this.on_scrap_college_data} className="btn btn-primary">Scrape</button>
                                </div>
                                <span>Scrape information about all colleges from CollegeData.com</span>
                            </div>
                        </div>
                        <div className="import" style={
                            {display: (this.state.current_tab === Admin.admin_tab_enum.IMPORT_DATA) ?
                                "" : "None"
                            }}>
                            <div>
                                <div>
                                    <h3>Import College Scorecard data file</h3>
                                     {/*<input type="file" onChange={this.on_submit_score_card_file} name="file"/>*/}
                                     <button disabled={this.state.import_data_score_card_disable} id={"submit_college_score_card_btn"} onClick={this.on_import_college_score_card} className="btn btn-primary">Import</button>
                                </div>
                                <span>Import information about all colleges from College Scorecard</span>
                            </div>

                            <div>
                                <div>
                                    <h3>Import student profile and application dataset</h3>
                                    <button disabled={this.state.import_student_data_disable} onClick={this.on_student_profile_import} className="btn btn-primary">Import</button>
                                </div>
                                <span>Import previously collected student profile and application dataset </span>
                            </div>
                        </div>
                        <div className="delete" style={
                            {display: (this.state.current_tab === Admin.admin_tab_enum.DELETE_ALL) ?
                                "" : "None"
                            }}>
                            <div>
                                <h3>Are you sure you want to delete all student profiles?</h3>
                                <button
                                    className="btn btn-danger"
                                    onClick={this.on_delete_all_click}
                                >
                                    Yes, Delete ALL
                                </button>
                            </div>
                        </div>
                        <div className="review" style={
                            {display: (this.state.current_tab === Admin.admin_tab_enum.REVIEW) ?
                                "" : "None"
                            }}>
                            <div>
                                <button style={{marginRight: "2%"}} onClick={this.on_still_questionable} className="btn btn-danger">Still Questionable</button>
                                <button onClick={this.on_acceptable} className="btn btn-primary">Acceptable</button>
                            </div>
                            <div className="list-group" id="questionable-list">

                                {reviews}

                            </div>
                            <nav>
                            {/* Initially, there should be no tags inside the tag below. */}
                            <ul className="pagination" id="pagination">
                                {/* "active" class below means the current active page button  */}
                                {/* first page button must be active in default after completing search */}
                                {
                                    page_lists
                                }
                            </ul>
                        </nav>
                        </div>
                    </div>
                </div>
                </div>
            </AdminProvider>
        );
    }
}


export default Admin;
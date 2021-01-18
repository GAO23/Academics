import React from "react";
import '../../gui/css/search_college.css';
import CollegeItem from "./CollegeItem";
import {DropdownButton, Dropdown} from "react-bootstrap";
import {EXPIRED_MSG, NOT_AUTHENTICATED_ERROR, SERVER_URL, STATUS_OK} from "../../common/Constants";
import FindSimilarApplicantsModal from "./FindSimilarApplicantsModal";
import Authenticator from "../../common/Authenticator";

const RECOMMENDED_COLLEGE_ENDPOINT = "/get_college_list";
const MAJOR_ENDPOINT = "/all_majors";
const SIMILAR_ENDPOINT = "/get_similar_profiles";

class SearchCollege extends React.Component{

    static not_found_style={
        backgroundColor: "white",
        textAlign: "center"
    }

    static fixes ={
        flexWrap: "wrap",
        display: "inline-flex"
    }

    static show = {
        display : "flex",
        verticalAlign:"top"
    }

    static hide = {
        display: "None"
    }

    static sort_enum = {
        NAME: "name",
        COST: "cost",
        RANKING: "ranking",
        RECOMMENDATION: "recommendation",
        ADMISSION: "admission"
    }



    constructor(props) {
        super(props);
        this.state = {
            show_filter: false,
            current_page_num: 1,
            college_list: [],
            majors_list: [],
            not_found: false,
            filter_data: {
                name: "",
                admission_rate: {min: "", max: ""},
                location: '-',
                sat_ebrw: {min: '', max: ''},
                max_ranking: '-',
                size: '-',
                sat_math: {min: '', max: ''},
                major: {left: '-', right: '-'},
                max_tuition: '',
                act: {min: '', max: ''},
                policy: "strict",
                sort: SearchCollege.sort_enum.NAME
            },
            majors: {left: [], right: []},
            show_modal: false,
            current_modal_collage_profiles: []
        }
        this.button_list = [];
        this.filter_drop_down_clicked = this.filter_drop_down_clicked.bind(this);
        this.slider_input = this.slider_input.bind(this);
        this.page_clicked = this.page_clicked.bind(this);
        this.get_colleges = this.get_colleges.bind(this);
        this.fetch_new_college_list = this.fetch_new_college_list.bind(this);
        this.search_clicked = this.search_clicked.bind(this);
        this.sort_clicked = this.sort_clicked.bind(this);
        this.fetch_majors = this.fetch_majors.bind(this);
        this.get_majors = this.get_majors.bind(this);
        this.input_check = this.input_check.bind(this);
        this.show_collage_modal = this.show_collage_modal.bind(this);
        this.set_current_modal_college = this.set_current_modal_college.bind(this);
        this.fetch_similar_profiles = this.fetch_similar_profiles.bind(this);
    }

    sort_clicked(event){
        if(event === SearchCollege.sort_enum.RECOMMENDATION){
            if(typeof this.old_recommendation !== 'undefined') {
                    this.setState({current_page_num: 1, college_list: this.old_recommendation, filter_data: {...this.state.filter_data, sort: SearchCollege.sort_enum.RECOMMENDATION}});
                    return;
                }
        }

        this.state.filter_data.sort = event;
        this.fetch_new_college_list();
    }


    filter_drop_down_clicked(event){
        this.setState({show_filter: !this.state.show_filter});
    }

    slider_input(event){
        let new_value = event.target.value;
        if (new_value === "0"){
            new_value = "-";
        }else{
            new_value = (new_value === "601") ? "600+" : new_value;
        }
        this.setState({filter_data:{...this.state.filter_data, max_ranking: new_value}});
    }

    async set_current_modal_college(name){
        let result =  await this.fetch_similar_profiles(name);
        this.setState({current_modal_collage_profiles: result});
    }

    page_clicked(event){
       let button = event.target;
       let number = button.innerHTML;
       this.setState({current_page_num: parseInt(number)})
    }

     is_whole_number(n) {
        let result = (n - Math.floor(n)) !== 0;

        if (result)
            return false;
        else
            return true;
    }



    // this will depend on the colleges
    get_page_buttons(){
        let page_list = [];
        let new_button_list = [];
        let max_pages = this.state.college_list.length / 10;
        if(!this.is_whole_number(max_pages)) {
            max_pages = Math.floor(max_pages) + 1;
        }
        let next_ten_pages = this.state.current_page_num + 9;
        let old_beginning = this.button_list[0];
        let old_end = this.button_list[this.button_list.length - 1];
        if(this.state.college_list.length <= 10) {
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
        if(this.state.college_list.length === 0){
            this.button_list = [];
            return [];
        }
        this.button_list = new_button_list;
        return page_list;
    }


    async fetch_majors(){
        try{
            let response = await fetch(SERVER_URL + MAJOR_ENDPOINT);
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            this.setState({majors_list: response_json.majors});
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

    input_check(){
        let filter_data = this.state.filter_data;
        if(filter_data.max_tuition !== "" ){
            if(filter_data.max_tuition <= 0 || filter_data.max_tuition > 100) throw new Error("cost of attendance should be 1-100");
        }
        if(filter_data.sat_ebrw.min !== ""){
            if(filter_data.sat_ebrw.min < 200 || filter_data.sat_ebrw.min > 800) throw  new Error("sat ebrw min should be 200-800");
        }

        if(filter_data.sat_ebrw.max !== ""){
            if( filter_data.sat_ebrw.max < 200 || filter_data.sat_ebrw.max > 800) throw  new Error("sat ebrw max should be 200-800");
        }

        if(filter_data.sat_math.min !== ""){
            if(filter_data.sat_math.min < 200 || filter_data.sat_math.min > 800 ) throw  new Error("sat math min should be 200-800");
        }

        if(filter_data.sat_math.max !== ""){
            if(filter_data.sat_math.max < 200 || filter_data.sat_math.max > 800 ) throw  new Error("sat math max should be 200-800");
        }

        if(filter_data.act.min !== ""){
            if(filter_data.act.min < 1 || filter_data.act.min > 36) throw  new Error("act min should be 1-36");
        }
        if(filter_data.act.max !== ""){
            if(filter_data.act.max < 1 || filter_data.act.max > 36) throw new Error("act max should be 1-36");
        }
        if(filter_data.admission_rate.min !== ""){
            if(filter_data.admission_rate.min < 0 || filter_data.admission_rate.min > 100) throw  new Error("admission rate min should be 0-100");
        }
        if(filter_data.admission_rate.max !== "") {
            if(filter_data.admission_rate.max > 100 || filter_data.admission_rate.max < 0) throw  new Error("admission rate max should be 0-100");
        }
    }


    async fetch_new_college_list(){
        try{
            // deep copy
            this.input_check();
            let body = JSON.parse(JSON.stringify(this.state.filter_data));
            Object.keys(body).map(function(key, index) {
                    let min = body[key].min;
                    let max = body[key].max;
                    let left = body[key].left;
                    let right = body[key].right;
                    if(typeof left !== 'undefined'){
                        body[key].left = (left === '' || left === '-') ? null : left;

                    }
                    if(typeof right !== 'undefined'){
                        body[key].right = (right === '' || right === '-') ? null : right;
                    }

                    if(typeof min !== 'undefined'){
                        body[key].min = (min === '' || min === '-') ? null : parseInt(min);
                    }
                    if(typeof max !== 'undefined') {
                        body[key].max = (max === '' || max === '-') ? null : parseInt(max);
                    }

                    body[key] = (body[key] === '' || body[key] === '-') ? null : body[key];

            });
            body.max_ranking = (body.max_ranking == null) ? null : parseInt(body.max_ranking);
            body.max_tuition = (body.max_tuition == null) ? null : parseInt(body.max_tuition) * 1000;
            console.log(body);
            let response = await fetch(
                SERVER_URL + RECOMMENDED_COLLEGE_ENDPOINT,
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
            if(this.state.filter_data.sort === SearchCollege.sort_enum.RECOMMENDATION) this.old_recommendation = response_json.list;
            if(response_json.colleges.length === 0)
                this.state.not_found = true;
            else
                this.state.not_found = false;
            this.setState({current_page_num: 1, college_list: response_json.colleges});
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

    show_collage_modal(){
        this.setState({show_modal: !this.state.show_modal});
    }


    get_colleges(){
        if(this.state.not_found){
            return (<h1 style={SearchCollege.not_found_style}>No results found</h1>);
        }

        let list = [];
        let beginning = (this.state.current_page_num === 1) ? 0 : (this.state.current_page_num - 1) * 10;
        let end_index = beginning + 10;

        for(let i = beginning; (i < this.state.college_list.length) && (i < end_index); i++){
            let college = this.state.college_list[i];
            list.push(
               <CollegeItem key={`college_key-${i}`} data= {
                   {
                       name: (college.name == null) ? "-" : college.name,
                       state: (college.state == null) ? "-" : college.state,
                       institution: (college.institution == null) ? "-" : college.institution,
                       admission_rate: (college.admission_rate == null) ? "-" : college.admission_rate,
                       tuition: (college.tuition == null) ? "-" : college.tuition,
                       debt: (college.debt == null) ? "-" : college.debt,
                       completion: (college.completion_rate == null) ? "-" : college.completion_rate,
                       ranking: (college.ranking == null) ? "-": college.ranking,
                       size: (college.size == null) ? "-" : college.size,
                       college_id: (college.college_id == null) ? "-" : college.college_id,
                       recommendation : (college.recommendation == null) ? "-": college.recommendation
                    }
               }
               show_collage_modal={this.show_collage_modal}
               set_current_modal_college={this.set_current_modal_college}
               current_sort={this.state.filter_data.sort}
               />
            )
        }
        return list;
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


    set_none_scores_filter_data(data){
        this.setState({filter_data: {...this.state.filter_data, ...data}});
    }

    search_clicked(event){
        this.state.filter_data.sort = SearchCollege.sort_enum.NAME;
        this.fetch_new_college_list().then(()=>{}).catch();
        this.setState({show_filter: false});
    }

    async componentDidMount() {
        await this.fetch_majors();
    }

    async fetch_similar_profiles(name){
      try{
            let response = await fetch(
                SERVER_URL + SIMILAR_ENDPOINT,
                {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({college_name: name})
                }
            );
            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            return response_json.profiles;
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
            return [];
        }
    }


    render() {
        let colleges = this.get_colleges();
        let page_lists = this.get_page_buttons();
        let majors = this.get_majors();
        return (
            <div className="right-content">
                <div className="wrap-search-result">
                    <div className="wrap-search-filters">
                        <div className="search-box">
                            <div className="input-group mb-3">
                                <input onChange={(event)=>{this.set_none_scores_filter_data({name: event.target.value})}}
                                       value={this.state.filter_data.name}
                                       type="text" className="form-control shadow-none"
                                       placeholder="College name"
                                       aria-label="College Name" aria-describedby="search-btn"
                                />
                                <div className="input-group-append">
                                    <button onClick={this.search_clicked} className="btn btn-outline-secondary shadow-none" type="button"
                                            id="search-btn">Search
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div className="filters-box">
                            <div className="filters-dropdown">
                                <button className="btn btn-secondary shadow-none" type="button" id="filters-dropdown-btn" onClick={this.filter_drop_down_clicked}>
                                    Filters
                                </button>
                                <div style={(this.state.show_filter) ? SearchCollege.show : SearchCollege.hide} id="search-filters-dropdown-content">
                                    <table>
                                        <tbody>
                                        <tr>
                                            <td><b>Admission rate (%)</b>
                                                <div style={SearchCollege.fixes} className="wrap-filter range">
                                                    <input onChange={(event)=>{
                                                        let new_min = event.target.value;
                                                        let admissionRate = this.state.filter_data.admission_rate;
                                                        this.setState({filter_data: {...this.state.filter_data, admission_rate: {...admissionRate, min: new_min}}})
                                                    }}
                                                           value={this.state.filter_data.admission_rate.min} type="number" className="form-control shadow-none" id="sat-ebrw-min" placeholder="0 - 100" min={200} max={800}
                                                    />
                                                    &nbsp;-&nbsp;
                                                    <input onChange={(event)=>{
                                                        let new_max = event.target.value;
                                                        let admissionRate = this.state.filter_data.admission_rate;
                                                        this.setState({filter_data: {...this.state.filter_data, admission_rate: {...admissionRate, max: new_max}}})
                                                    }}
                                                           value={this.state.filter_data.admission_rate.max} type="number" className="form-control shadow-none" id="sat-ebrw-max" placeholder="0 - 100" min={200} max={800}
                                                    />
                                                </div>
                                            </td>
                                            <td><b>Max ranking</b><span id="ranking-val">{this.state.filter_data.max_ranking}</span>
                                                <div className="wrap-filter">
                                                    {/* 0 means no preference, 601 means 600 + */}
                                                    <input type="range" min={0} max={601} defaultValue={0}
                                                           className="slider" id="ranking"
                                                           onChange={this.slider_input}
                                                    />
                                                </div>
                                            </td>
                                            <td><b>Major</b>
                                                <div  className="wrap-filter">
                                                    <select  id="major1" defaultValue={this.state.filter_data.major.left}
                                                            onChange={(event)=>{
                                                                    console.log(event.target.value);
                                                                    this.setState({filter_data: {...this.state.filter_data, major: {left: this.state.filter_data.major.left, right: event.target.value}}});
                                                                }
                                                            }
                                                    >
                                                        {/* - means no preference */}
                                                        <option value="-">-</option>
                                                        {majors}
                                                        {/* Frontend should add option tags with the major names from here */}
                                                    </select>
                                                    &amp;
                                                    <select style={SearchCollege.fixes} id="major2" defaultValue={this.state.filter_data.major.right}
                                                        onChange={(event)=>{
                                                                console.log(event.target.value);
                                                                    this.setState({filter_data: {...this.state.filter_data, major: {right: this.state.filter_data.major.right, left: event.target.value}}});
                                                                }
                                                            }
                                                    >
                                                        {/* - means no preference */}
                                                        <option value="-">-</option>
                                                        {majors}
                                                        {/* Frontend should add option tags with the major names from here */}
                                                    </select>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr style={{height: '15px'}}/>
                                        <tr>
                                            <td><b>Location</b>
                                                <div className="wrap-filter">
                                                    <select id="location"
                                                        defaultValue={this.state.filter_data.location}
                                                        onChange={event => {
                                                                this.setState({filter_data: {
                                                                        ...this.state.filter_data, location: event.target.value
                                                                    }})
                                                            }
                                                        }
                                                    >
                                                        {/* - means no preference */}
                                                        <option value="-">-</option>
                                                        <option value="Midwest">Midwest</option>
                                                        <option value="Northeast">Northeast</option>
                                                        <option value="South">South</option>
                                                        <option value="West">West</option>
                                                        <option value="Other">Other</option>
                                                    </select>
                                                </div>
                                            </td>
                                            <td><b>Size</b>
                                                <div className="wrap-filter" >
                                                    <select id="size"
                                                        defaultValue={this.state.filter_data.size}
                                                        onChange={event => {
                                                                this.setState({filter_data: {
                                                                        ...this.state.filter_data, size: event.target.value
                                                                    }})
                                                            }
                                                        }
                                                    >
                                                        {/* - means no preference */}
                                                        <option value="-">-</option>
                                                        <option value="small">small (&lt; 2,000)</option>
                                                        <option value="medium">medium (2,000 - 15,000)</option>
                                                        <option value="large">large (&gt; 15,000)</option>
                                                    </select>
                                                </div>
                                            </td>
                                            <td><b>Max cost of attendance (k $)</b>
                                                <div className="wrap-filter">
                                                    <input type="number" className="form-control shadow-none"
                                                           id="cost-of-attendance" placeholder="1 - 100" min="1"
                                                           max="100"
                                                           onChange={(event) =>{
                                                                    this.set_none_scores_filter_data({max_tuition: event.target.value})
                                                                }
                                                           }
                                                    />
                                                </div>
                                            </td>
                                            <td></td>
                                        </tr>
                                        <tr style={{height: '15px'}}/>
                                        <tr>
                                            {/* frontend javascript should check whether the min is less than or eqal to max */}
                                            <td><b>SAT EBRW score</b>
                                                <div style={SearchCollege.fixes} className="wrap-filter range">
                                                    <input onChange={(event)=>{
                                                        let new_min = event.target.value;
                                                        let old_sat_ebrw = this.state.filter_data.sat_ebrw;
                                                        this.setState({filter_data: {...this.state.filter_data, sat_ebrw: {...old_sat_ebrw, min: new_min}}})
                                                    }}
                                                           value={this.state.filter_data.sat_ebrw.min} type="number" className="form-control shadow-none" id="sat-ebrw-min" placeholder="200 - 800" min={200} max={800}
                                                    />
                                                    -
                                                    <input onChange={(event)=>{
                                                        let new_max = event.target.value;
                                                        let old_sat_ebrw = this.state.filter_data.sat_ebrw;
                                                        this.setState({filter_data: {...this.state.filter_data, sat_ebrw: {...old_sat_ebrw, max: new_max}}})
                                                    }}
                                                           value={this.state.filter_data.sat_ebrw.max} type="number" className="form-control shadow-none" id="sat-ebrw-max" placeholder="200 - 800" min={200} max={800}
                                                    />
                                                </div>
                                            </td>
                                            <td><b>SAT Math score</b>
                                                <div style={SearchCollege.fixes} className="wrap-filter">
                                                     <input onChange={(event)=>{
                                                        let new_min = event.target.value;
                                                        let old_sat_math = this.state.filter_data.sat_math;
                                                        this.setState({filter_data: {...this.state.filter_data, sat_math: {...old_sat_math, min: new_min}}})
                                                    }}
                                                            value={this.state.filter_data.sat_math.min}
                                                            type="number" className="form-control shadow-none"
                                                            id="sat-math-min" placeholder="200 - 800" min={200}
                                                            max={800}
                                                     />
                                                     -
                                                     <input onChange={(event)=>{
                                                        let new_max = event.target.value;
                                                        let old_sat_math = this.state.filter_data.sat_math;
                                                        this.setState({filter_data: {...this.state.filter_data, sat_math: {...old_sat_math, max: new_max}}})
                                                    }}
                                                            value={this.state.filter_data.sat_math.max}
                                                            type="number" className="form-control shadow-none"
                                                            id="sat-math-max" placeholder="200 - 800" min={200}
                                                            max={800}
                                                     />
                                                </div>
                                            </td>
                                            <td><b>ACT Composite score</b><br/>
                                                <div style={SearchCollege.fixes} className="wrap-filter">
                                                     <input onChange={(event)=>{
                                                        let new_min = event.target.value;
                                                        let old_act = this.state.filter_data.act;
                                                        this.setState({filter_data: {...this.state.filter_data, act: {...old_act, min: new_min}}})
                                                    }}
                                                            value={this.state.filter_data.act.min}
                                                            type="number" className="form-control shadow-none"
                                                            id="act-composite-min" placeholder="1 - 36" min={1}
                                                            max={36}
                                                     />
                                                     &nbsp;-&nbsp;
                                                     <input onChange={(event)=>{
                                                        let new_max = event.target.value;
                                                        let old_act = this.state.filter_data.act;
                                                        this.setState({filter_data: {...this.state.filter_data, act: {...old_act, max: new_max}}})
                                                    }}
                                                            value={this.state.filter_data.act.max}
                                                            type="number" className="form-control shadow-none"
                                                            id="act-composite-max" placeholder="1 - 36" min={1}
                                                            max={36}
                                                     />
                                                </div>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div className="filters-radio">
                                <div className="custom-control custom-radio">
                                  <input type="radio" id="strict" name="customRadio" className="custom-control-input" checked={this.state.filter_data.policy === 'strict'}
                                    onChange={event => {
                                        this.set_none_scores_filter_data({policy: "strict"});
                                    }}
                                  />
                                  <label className="custom-control-label" htmlFor="strict">Strict</label>
                                </div>
                                <div className="custom-control custom-radio">
                                  <input type="radio" id="lax" name="customRadio" className="custom-control-input"
                                    onChange={event => {
                                        this.set_none_scores_filter_data({policy: "lax"});
                                    }}
                                  />
                                  <label className="custom-control-label" htmlFor="lax">Lax</label>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="wrap-result">
                        <div className="search-result-top">
                            <span className="result-text" style={{display: ((!this.state.not_found) && (this.state.college_list.length === 0)) ? "None" : ''}}>Results</span>

                            {
                                (this.state.college_list.length > 0) ?
                                    <DropdownButton id="dropdown-basic-button" title="Sort By" onSelect={this.sort_clicked}>
                                      <Dropdown.Item eventKey={SearchCollege.sort_enum.NAME}>College name - Alphabetical Order</Dropdown.Item>
                                      <Dropdown.Item eventKey={SearchCollege.sort_enum.COST}>Cost of attendance - Low to High</Dropdown.Item>
                                      <Dropdown.Item eventKey={SearchCollege.sort_enum.RANKING}>Ranking - High to Low</Dropdown.Item>
                                        <Dropdown.Item eventKey={SearchCollege.sort_enum.ADMISSION}>Admission rate - High to Low</Dropdown.Item>
                                        <Dropdown.Item eventKey={SearchCollege.sort_enum.RECOMMENDATION}>Recommendation score - High to Low</Dropdown.Item>
                                    </DropdownButton>
                                    :
                                    null
                            }

                        </div>
                        {/* Initially, there should be no tags inside the tag below. */}
                        {/* Frontend ajax should add tags with data inside the tag below */}
                        <div className="list-group" id="college-list">
                            {
                                colleges
                            }
                        </div>

                        <FindSimilarApplicantsModal current_modal_collage_profiles={this.state.current_modal_collage_profiles} show_collage_modal={this.show_collage_modal} show={this.state.show_modal}/>

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
        )
    }
}

export default SearchCollege;
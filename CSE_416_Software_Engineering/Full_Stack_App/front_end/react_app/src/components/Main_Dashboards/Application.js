import React from "react";

class Application extends React.Component{

    static badge_enum = {
        WARNING: "badge badge-warning",
        DANGER: "badge badge-danger",
        SUCCESS: "badge badge-success"
    }

    constructor(props) {
        super(props);
        this.state = {
            carousel_display: {display: "none"},
            current_carousel_slide: 0
        }
        this.toggle_carousel = this.toggle_carousel.bind(this);
        this.next_carousel = this.next_carousel.bind(this);
        this.previous_carousel = this.previous_carousel.bind(this);
    }

    toggle_carousel(event){
        if(this.state.carousel_display.display === "none"){
            this.setState({carousel_display: {display: "flex"}});
        }else{
            this.setState({carousel_display: {display: "none"}});
        }
    }

    next_carousel(event){
        if((this.state.current_carousel_slide + 1) > 2){
            this.setState({current_carousel_slide : 0});
        }else{
            this.setState({current_carousel_slide : this.state.current_carousel_slide + 1});
        }
    }

    previous_carousel(event){
        if((this.state.current_carousel_slide - 1) < 0){
            this.setState({current_carousel_slide : 2});
        }else{
            this.setState({current_carousel_slide : this.state.current_carousel_slide - 1});
        }
    }

    get_badge_style(acceptance){
        if(acceptance.toLowerCase().includes("accept")) return Application.badge_enum.SUCCESS;
        if(acceptance.toLowerCase().includes("denied")) return Application.badge_enum.DANGER;
        if(acceptance.toLowerCase().includes("deferred")) return Application.badge_enum.WARNING;
        if(acceptance.toLowerCase().includes("wait")) return Application.badge_enum.WARNING;
        if(acceptance.toLowerCase().includes("withdraw")) return Application.badge_enum.DANGER;
        if(acceptance.toLowerCase().includes("pending")) return Application.badge_enum.WARNING;
    }


    render() {
        let {btn_info,  personal, sat2, act} = this.props;
        let style = this.get_badge_style(btn_info.acceptance);
        return (
            <React.Fragment>
                <button className="list-group-item list-group-item-action" onClick={this.toggle_carousel}>
                    {/* student's username should be included inside h5 tag */}
                    <h5 className="username">{btn_info.username}<br/>
                        {/* accpeted: badge-success | denied: badge-danger | rest: badge-warning */}
                        <span className={style}>{btn_info.acceptance}</span>
                    </h5>
                    {/* high school name should be one the left side of br tag
                    high school city  should be combined with high school state by comma and this combination should be on the right side of br tag and inside h5 tag */}
                    <h5 className="high-school">{btn_info.high_school}<br/>{btn_info.high_school_location}</h5>
                </button>
                {/* "div" tag's "id" property value should be the student's username*/}
                <div id="username1" style={this.state.carousel_display} className="carousel slide">
                    <ol className="carousel-indicators">
                        {/* every "li" tags' "data-target" property values should be the student's username*/}
                        <li
                            className={(this.state.current_carousel_slide === 0) ? "active" : ""}
                            onClick={event => {this.setState({current_carousel_slide: 0})}}
                        />
                        <li
                            className={(this.state.current_carousel_slide === 1) ? "active" : ""}
                            onClick={event => {this.setState({current_carousel_slide: 1})}}
                        />
                        <li
                            className={(this.state.current_carousel_slide === 2) ? "active" : ""}
                            onClick={event => {this.setState({current_carousel_slide: 2})}}
                        />
                    </ol>
                    <div className="carousel-inner">
                        <div className={`carousel-item personal ${(this.state.current_carousel_slide === 0) ? "active" : ""}`}>
                            <table>
                                <tbody>
                                <tr>
                                    {/* add data in b tags */}
                                    {/* if there is no data, add dash in b tags */}
                                    <td><b>{personal.state}</b><br/><label>Residence state</label></td>
                                    <td><b>{personal.math}</b><br/><label>SAT Math</label></td>
                                    <td><b>{personal.ap}</b><br/><label>APs Passed</label></td>
                                    <td><b>{personal.majors1}</b><br/><label>Major 1</label>
                                    </td>
                                </tr>
                                <tr>
                                    {/* add data in b tags */}
                                    {/* if there is no data, add dash in b tags */}
                                    <td><b>{personal.class}</b><br/><label>College class</label></td>
                                    <td><b>{personal.ebrw}</b><br/><label>SAT EBRW</label></td>
                                    <td><b>{personal.gpa}</b><br/><label>GPA</label></td>
                                    <td><b>{personal.majors2}</b><br/><label>Major
                                        2</label></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div className={`carousel-item sat2 ${(this.state.current_carousel_slide === 1) ? "active" : ""}`}>
                            <table>
                                <tbody>
                                <tr>
                                    {/* add data in b tags */}
                                    {/* if there is no data, add dash in b tags */}
                                    <td><b>{sat2.chemistry}</b><br/><label>SAT2 Chemistry</label></td>
                                    <td><b>{sat2.eco_bio}</b><br/><label>SAT2 Eco-Bio</label></td>
                                    <td><b>{sat2.literature}</b><br/><label>SAT2 Literature</label></td>
                                    <td><b>{sat2.mol_bio}</b><br/><label>SAT2 Mol-Bio</label></td>
                                </tr>
                                <tr>
                                    {/* add data in b tags */}
                                    {/* if there is no data, add dash in b tags */}
                                    <td><b>{sat2.math_I}</b><br/><label>SAT2 Math I</label></td>
                                    <td><b>{sat2.math_II}</b><br/><label>SAT2 Math II</label></td>
                                    <td><b>{sat2.physics}</b><br/><label>SAT2 Physics</label></td>
                                    <td><b>{sat2.us_history}</b><br/><label>SAT2 US History</label></td>
                                    <td><b>{sat2.world_history}</b><br/><label>SAT2 World History</label></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div className={`carousel-item act ${(this.state.current_carousel_slide === 2) ? "active" : ""}`}>
                            <table>
                                <tbody>
                                <tr>
                                    {/* add data in b tags */}
                                    {/* if there is no data, add dash in b tags */}
                                    <td key={"english"}><b>{act.english}</b><br/><label>ACT English</label></td>
                                    <td key={"math"}><b>{act.math}</b><br/><label>ACT Math</label></td>
                                    <td key={"reading"}><b>{act.reading}</b><br/><label>ACT Reading</label></td>
                                    <td key={"science"}><b>{act.science}</b><br/><label>ACT Science</label></td>
                                    <td key={"composite"}><b>{act.composite}</b><br/><label>ACT Composite</label></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    {/* "a" tag's "href" property value should be "#" + student's username*/}
                    <a
                        onClick={this.previous_carousel}
                        className="carousel-control-prev" role="button"
                        data-slide="prev"
                    >
                        <span className="carousel-control-prev-icon" aria-hidden="true"/>
                        <span className="sr-only">Previous</span>
                    </a>
                    {/* "a" tag's "href" property value should be "#" + student's username*/}
                    <a
                        onClick={this.next_carousel}
                        className="carousel-control-next" role="button"
                        data-slide="next"
                    >
                        <span className="carousel-control-next-icon" aria-hidden="true"/>
                        <span className="sr-only">Next</span>
                    </a>
                </div>
            </React.Fragment>
        )
    }
}

export default Application;

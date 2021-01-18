import React from "react";
import Logo from '../gui/img/logo.png';
import College from '../gui/img/college.png';
import Application from '../gui/img/application.png';
import PageImage from '../gui/img/mypage.png';
import Logout from '../gui/img/logout.png';
import SearchCollege from "./Main_Dashboards/SearchCollege";
import Authenticator from "../common/Authenticator";
import Track from "./Main_Dashboards/Track";
import {BrowserRouter, Route, Switch, Redirect, Link} from "react-router-dom";
import {NotFound} from "./NotFound";
import MyPage from "./Main_Dashboards/MyPage";
import {account_enum} from "../common/Authenticator";

class Main extends React.Component{
    static tab_enum =  {
            SEARCH: 'search',
            TRACK: 'track',
            MY_PAGE: 'my_page'
        }

    constructor(props) {
        super(props);
        this.logout_clicked = this.logout_clicked.bind(this);
        this.state = {
            current_tab: ""
        }
        this.update_active_tab = this.update_active_tab.bind(this);
    }

    async logout_clicked(){
        let username = Authenticator.getUserName();
        await Authenticator.logout();
        this.props.history.push({
                    pathname: '/login',
                    state: { username:username }
                });
    }

    tab_button_clicked(tab){
        this.setState({current_tab: tab})
        this.props.history.push({
                    pathname: `/main/${tab}`
                });
    }

    componentDidMount() {
        let paths = this.props.location.pathname.split('/');
        let last_url = paths[paths.length - 1];
        this.setState({current_tab: last_url});
    }

    update_active_tab(last_url){
        this.setState({current_tab: last_url});
    }

    render() {
        let from = this.props.location;
        if(Authenticator.getAccountType() === account_enum.ADMIN){
            return (<Redirect to={
                        {
                            pathname: "/admin",
                            state: {
                                from: from
                            }
                        }
                    }/>);
        }
        return(
            <div className="wrap-dashboard">
                <div className="left-menu">
                    <div className="wrap-logo btm-space-main"><img className="panel-logo" src={Logo} alt="logo"/></div>
                    <div>
                        {/* put chosen="true" to corresponding menu button */}
                        {/* for each menu button, replace # with appropriate end point  */}
                        <button chosen={`${(this.state.current_tab === Main.tab_enum.SEARCH)}`}
                                onClick={() => this.tab_button_clicked(Main.tab_enum.SEARCH)}>
                            <div className="wrap-icon"><img src={College} alt="college"/></div>
                            <div>Search College</div>
                        </button>
                        <button chosen={`${(this.state.current_tab === Main.tab_enum.TRACK)}`}
                                onClick={() => this.tab_button_clicked(Main.tab_enum.TRACK)}>
                            <div className="wrap-icon"><img src={Application} alt="application"/></div>
                            <div>Track Application</div>
                        </button>
                        <button chosen={`${(this.state.current_tab === Main.tab_enum.MY_PAGE)}`}
                                onClick={() => this.tab_button_clicked(Main.tab_enum.MY_PAGE)} >
                            <div className="wrap-icon"><img src={PageImage} alt="mypage"/></div>
                            <div>My Page</div>
                        </button>
                        <button onClick={this.logout_clicked} >
                            <div className="wrap-icon"><img src={Logout}  alt="logout"/></div>
                            <div>Log out</div>
                        </button>
                    </div>
                </div>
                <Switch>
                    <Route path='/main/search'  component={SearchCollege} />
                    <Route path='/main/track'  render={props => <Track {...props} update_active_tab={this.update_active_tab}/> } />
                    <Route path='/main/my_page' component={MyPage} />
                    <Route  component={NotFound}/>
                </Switch>

            </div>
        )
    }
}

export default Main;
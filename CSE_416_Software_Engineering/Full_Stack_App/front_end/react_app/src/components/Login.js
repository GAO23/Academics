import React from "react";
import '../gui/css/login_signup.css';
import {Link} from "react-router-dom";
import Logo from '../gui/img/logo.png';
import authenticator, {account_enum} from "../common/Authenticator";

class Login extends React.Component{
    constructor(props) {
        super(props);
        this.state = {
            invalid_msg: '',
            password: '',
            username: ''
        };
        this.setPassword = this.setPassword.bind(this);
        this.setUserName = this.setUserName.bind(this);
        this.login = this.login.bind(this);
    }

    login(event){
        event.preventDefault();
        console.log(this.state);
        authenticator.login(this.state.username, this.state.password).then(
            err =>{
                if(err){
                    this.setState({invalid_msg: err});
                } else{
                    if(authenticator.getAccountType() === account_enum.ADMIN){
                        this.props.history.push('/admin');
                    }else{
                        this.props.history.push('/main/search');
                    }

                }
            }
        ).catch(err =>{
                console.log(err.message);
            }
        )
    }

    setPassword(event){
        this.setState({password: event.target.value});
    }

    setUserName(event){
        this.setState({username: event.target.value});
    }

    componentDidMount(){
        if(this.props.location.state && this.props.location.state.username){
            this.setState({username: this.props.location.state.username}); // if user name is passed from the sign up
        }
    }

    render() {
        return (
            <div className = 'signup_login_container'>
                <div id="bg-outer">
                    <div className="wrap-form">
                        <form className="form">
                            <span className="form-title">Welcome to</span>
                            <div className="wrap-logo btm-space">
                                <img className="logo" src={Logo} alt="logo" />
                            </div>
                            <div className="wrap-form-input btm-space">
                                <input value={this.state.username} onChange={this.setUserName} type="text" name="username" placeholder="Username" minLength={4} maxLength={32} required />
                            </div>
                            <div className="wrap-form-input btm-space">
                                <input onChange={this.setPassword} value={this.state.password} type="password" name="password" placeholder="Password" minLength={8} maxLength={128} required />
                            </div>
                            <div className='invalid_msg'>{this.state.invalid_msg}</div>
                            <div>
                                <button onClick={this.login} className="btn login-button btn-primary login-btn" type="submit">Log in</button>
                            </div>
                            <Link to={{ pathname: '/signup'}}>Sign up</Link>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

export default Login;
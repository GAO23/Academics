import React from "react";
import '../gui/css/login_signup.css';
import Logo from '../gui/img/logo.png';
import {SERVER_URL} from "../common/Constants";

const SIGNUP_ENDPOINT = '/signup';

class Signup extends React.Component{
    constructor(props) {
        super(props);
        this.state = {
            password: '',
            username: '',
            err_msg: ''
        }
        this.setPassword = this.setPassword.bind(this);
        this.setUserName = this.setUserName.bind(this);
        this.register = this.register.bind(this);
    }

    setPassword(event){
        this.setState({password: event.target.value});
    }

    setUserName(event){
        this.setState({username: event.target.value});
    }

    async register(event){
        event.preventDefault();
        try{
            let re = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/g;
            if(this.state.username.length < 4){
                throw new Error('username is not long enough');
            }
            if(!this.state.password.match(re)) {
                throw new Error('Password not meet the requirement');
            }
            let response = await fetch(SERVER_URL + SIGNUP_ENDPOINT,
                {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({username: this.state.username , password: this.state.password})
                    }
                )

            if(response.status !== 200) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200){
                throw new Error(response_json.resuls);
            }else{
                alert("Successfully signed up. Redirecting to login page.")
                this.props.history.push({
                    pathname: '/login',
                    state: { username: this.state.username }
                });
            }
        }catch (err) {
            this.setState({err_msg: err.message});
            console.log(err);
        }
    }

    render() {
        return (
            <div className='signup_login_container'>
                <div id="bg-outer">
                    <div id="bg-inner">
                    </div>
                    <div className="wrap-form">
                        <form className="form">
                            <span className="form-title">Create Account for</span>
                            <div className="wrap-logo btm-space">
                                <img className="logo" src={Logo} alt="logo" />
                            </div>
                            <div className="btm-space">
                                <div className="wrap-form-input">
                                    <input onChange={this.setUserName} value={this.state.username} type="text" name="username" placeholder="Username" minLength={4} maxLength={32} required />
                                </div>
                                <p className="helper">* 4 - 32 characters</p>
                            </div>
                            <div className="btm-space">
                                <div className="wrap-form-input">
                                    <input onChange={this.setPassword} value={this.state.password} type="password" name="password" placeholder="Password" minLength={8} maxLength={128} required />
                                </div>
                                <p className="helper">* 8 - 128 characters</p>
                                <p className="helper">* At least 1 number, 1 special character, and both uppercase and lowercase letters</p>
                            </div>
                            <div>
                                <div className='invalid_msg'>{this.state.err_msg}</div>
                                <button onClick={this.register} className="login-button btn btn-primary" type="submit">Sign up</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

export default Signup;
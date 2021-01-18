import {SERVER_URL, STATUS_OK, DEBUGGING} from "./Constants";

const LOGIN_ENDPOINT = '/login';
const ALIVE_ENDPOITN = '/alive';
const LOGOUT_ENDPOINT = '/logout';

let account_enum = {
        STUDENT: "Student",
        ADMIN: "Admin"
    }

class Authenticator{

    constructor() {
        this.authenticated = (DEBUGGING) ? true : false;
        this.userID = "";
        this.account = "";
    }


    // for logging in users only
    async login(username, password){
        try{
            let body = {username: username, password: password};
            let response = await fetch(
                SERVER_URL + LOGIN_ENDPOINT,
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
            if(!response.ok) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.status !== 200) throw new Error(response_json.result);
            this.authenticated = true;
            this.userID = username;
            this.account = response_json.account;
        } catch (err) {
            return err.message;
        }
    }

    // unimplemented
    async logout(){
        try{
            let response = await fetch(
                SERVER_URL + LOGOUT_ENDPOINT,
                {
                    method: 'POST',
                    credentials: 'include',
                    header:{
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                }
            );
            if(response.status != STATUS_OK) throw new Error(response.statusText);
            let response_json = await response.json();
            if (response_json.status !== STATUS_OK) throw new Error(response_json.result);
            this.authenticated = false;
            this.userID = "";
        }catch (err) {
            alert(`Failed to logout in server: ${err.message}\nRedireting and logging out anyways`);
            this.userID = "";
            this.authenticated = false;
        }
    }

    async checkAlive(){
        try{
           let response = await fetch(
                SERVER_URL + ALIVE_ENDPOITN,
                {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include'
                }
            );
            if(response.status !== STATUS_OK) throw new Error(response.statusText);
            let response_json = await response.json();
            this.authenticated = (response_json.status === STATUS_OK);
            if(response_json.status === STATUS_OK) this.account = response_json.account;
        }catch (err) {
            alert(`Failed to check /api/alive, error msg: ${err.message}\nDefaulting loggined to ${this.authenticated}`);
        }
    }

    isAuthenticated(){
        return this.authenticated;
    }

    getUserName(){
        return this.userID;
    }

    getAccountType(){
        return this.account;
    }

    expiredSession(){
        this.userID = "";
        this.account = "";
        this.authenticated = false;
    }
}

// exporting a new instance as singleton
export default new Authenticator();
export {account_enum as account_enum};
import React from "react";
import {Route, Redirect} from 'react-router-dom';
import authenticator from "./Authenticator";

export const IfAuthenticatedRoute = ({session, path, component: Component, ...rest}) => {
    let authenticated = authenticator.isAuthenticated();
    if(session === null){
        return (
            // show blank page until check alive resolves
            <div />
        )
    }else{
        return(
        <Route {...path} render={
            (props) => {
               return (authenticated) ? <Redirect to={
                   {
                       pathname: "/main",
                       state: {
                           from: props.location
                       }
                   }
               } /> :
                   <Component {...props} {...rest} />
            }
        }
        />
    );
    }

};
import React from "react";
import Modal from 'react-bootstrap/Modal'
import {EXPIRED_MSG, NOT_AUTHENTICATED_ERROR, SERVER_URL, STATUS_OK} from "../../common/Constants";
import Plotly from "plotly.js"
import createPlotlyComponent from 'react-plotly.js/factory';
import Authenticator from "../../common/Authenticator";
const Plot = createPlotlyComponent(Plotly);

const PLOT_ENDPOINT = "/track_applications_plot";

class ScatterPlotModal extends React.Component{

   static graph_type_enum = {
       SAT: "SAT",
       ACT: "ACT",
       BOTH: "SAT_ACT"
   }



    constructor(props) {
        super(props);
        this.state = {
            scatter_plots: [],
            graph_type: ScatterPlotModal.graph_type_enum.ACT,
            empty: false,
            color: []
        }
        this.fetch_data = this.fetch_data.bind(this);
        this.on_button_click = this.on_button_click.bind(this);
        this.get_scatter_plot = this.get_scatter_plot.bind(this);
        this.get_title = this.get_title.bind(this);
    }

    static center_title = {
        marginLeft: "37.5%"
    }

    get_title(type){
       if(type === ScatterPlotModal.graph_type_enum.SAT){
           return "SAT(Math + EBRW)";
       }
       if(type === ScatterPlotModal.graph_type_enum.ACT){
           return "ACT Composite";
       }
       if(type === ScatterPlotModal.graph_type_enum.BOTH){
           return "SAT2+SAT/ACT";
       }
    }

    async fetch_data(type){
        try{
            let body = {...this.props.input_json, test_type: type};
            console.log(body);
            let response = await fetch(
                SERVER_URL + PLOT_ENDPOINT,
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
            if(response.status != STATUS_OK) throw new Error(response.statusText);
            let response_json = await response.json();
            if(response_json.result !== "OK") throw new Error(response_json.result);
            this.state.scatter_plots = response_json.coordinates;
            this.state.color = response_json.coordinates.map((element)=>{
                    if(element.status.toLowerCase().includes("accept")) return "green";
                    if(element.status.toLowerCase().includes("denied")) return "red";
                    return "yellow";
                }
            );
            this.state.empty = (response_json.coordinates.length === 0) ? true : false;
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

    async on_button_click(type){
        await this.fetch_data(type);
        this.setState({graph_type: type});
    }

    get_scatter_plot(){
       if(this.state.empty) return "no coordinates found";
       if(this.state.scatter_plots.length === 0) return "Click on a button to see scatter plot";
       let total_x = 0;
       let total_y = 0;
       let x = this.state.scatter_plots.map(function (coordinate) {
           total_x += coordinate.x;
           return coordinate.x;
        });

       let y = this.state.scatter_plots.map(function (coordinate) {
           total_y += coordinate.y;
           return coordinate.y;
        });
       let avg_x = total_x / x.length;
       let avg_y = total_y / y.length;
       console.log(x);
       console.log(y);

       return(
           <Plot
               data={[
                  {
                      x: x,
                      y: y,
                      type: 'scatter',
                      mode: 'markers+text',
                      marker: {color: this.state.color}
                  },
               ]}
                layout={
                    {
                        xaxis: {title: "Test scores"},
                        yaxis: {title: "GPA"},
                        width: 450,
                        height: 450,
                        title: this.get_title(this.state.graph_type),
                        shapes: [
                        {
                            type: 'line',
                            xref: 'paper',
                            x0: 0,
                            y0: avg_y,
                            x1: 1,
                            y1: avg_y,
                            line:{
                                color: 'black',
                                width: 2,
                                dash:'dot'
                            }
                        },
                            {
                            type: 'line',
                            xref: 'x',
                            x0: avg_x,
                            y0: 0,
                            x1: avg_x,
                            y1: 4,
                            line:{
                                color: 'blue',
                                width: 2,
                                dash:'dot'
                            }
                        }
                        ]
                    }
                }
            />
       );
    }

    render() {
        let graph = this.get_scatter_plot();
        return (
            <Modal show={this.props.show} onHide={()=>{
                this.setState({
                scatter_plots: [],
                graph_type: ScatterPlotModal.graph_type_enum.ACT,
                empty: false,
                color: []
                });
                this.props.on_hide();
            }} aria-labelledby="plot-modal-label" centered>
                <div className="modal-content">
                    <Modal.Header closeButton>
                        <Modal.Title  style={ScatterPlotModal.center_title}>Scatterplot</Modal.Title>
                    </Modal.Header>
                    <div className="modal-body">
                      <div id="plot-modal-buttons">
                        <button onClick={(event => {this.on_button_click(ScatterPlotModal.graph_type_enum.SAT)})} style={{marginRight: "2%"}} type="button"  className="btn btn-primary shadow-none plot-modal-option-btn">SAT (Math+EBRW)</button>
                        <button onClick={(event => {this.on_button_click(ScatterPlotModal.graph_type_enum.ACT)})} style={{marginRight: "2%"}} type="button"  className="btn btn-primary shadow-none plot-modal-option-btn">ACT Composite</button>
                        <button onClick={(event => {this.on_button_click(ScatterPlotModal.graph_type_enum.BOTH)})} type="button"  className="btn btn-primary shadow-none plot-modal-option-btn">SAT2+SAT/ACT</button>
                      </div>
                      <div style={{textAlign: "center"}} id="plot-modal-scatterplot">
                        {graph}
                      </div>
                    </div>
                  </div>
            </Modal>
        )
    }
}

export default ScatterPlotModal;
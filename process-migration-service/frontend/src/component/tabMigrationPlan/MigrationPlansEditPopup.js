import React from "react";

import { Button } from "patternfly-react";
import { Modal } from "patternfly-react";
import { Icon } from "patternfly-react";
import { OverlayTrigger } from "patternfly-react";
import { Tooltip } from "patternfly-react";
export default class MigrationPlansEditPopup extends React.Component {
  constructor() {
    super();
    this.state = { showEditPlanPopup: false };
  }
  closeEditPlanPopup = () => {
    this.setState({ showEditPlanPopup: false });
  };
  openEditPlanPopup = () => {
    this.setState({ showEditPlanPopup: true });
  };

  submit = () => {
    if (this.props.actionName == "Import Plan") {
      //this component is used as "Import Plan"
      var input = document.getElementById("planEditArea");
      var value = input.value;
      this.props.addPlan(value);
      this.props.retrieveAllPlans();
    } else {
      //this pop-up is used as "Export Plan", so copy the plan to clipboard for Export
      input = document.getElementById("planEditArea");
      value = input.value;
      navigator.clipboard.writeText(value);
    }
    this.setState({ showEditPlanPopup: false });
  };

  render() {
    const tooltipExport = (
      <Tooltip id="tooltip">
        <div>Export Migration Plan</div>
      </Tooltip>
    );

    const defaultBody = (
      <form className="form-horizontal">
        <div className="form-group">
          <label className="col-sm-3 control-label" htmlFor="textInput">
            Migration Plan
          </label>
          <div className="col-sm-9">
            <textarea
              className="form-control"
              id="planEditArea"
              name="planEditArea"
              rows="10"
              defaultValue={this.props.content}
            />
          </div>
        </div>
      </form>
    );

    function DisplayButtonOrLabel(props) {
      if (props.actionName == "Import Plan") {
        return (
          <Button bsStyle="default" onClick={props.openEditPlanPopup}>
            {props.actionName}
          </Button>
        );
      } else {
        return (
          <OverlayTrigger overlay={tooltipExport} placement={"bottom"}>
            <Button bsStyle="link" onClick={props.openEditPlanPopup}>
              <Icon type="pf" name="export" />
            </Button>
          </OverlayTrigger>
        );
      }
    }

    return (
      <span>
        <DisplayButtonOrLabel
          actionName={this.props.actionName}
          openEditPlanPopup={this.openEditPlanPopup}
        />

        <Modal
          show={this.state.showEditPlanPopup}
          onHide={this.closeEditPlanPopup}
          size="lg"
        >
          <Modal.Header>
            <Modal.CloseButton onClick={this.closeEditPlanPopup} />
            <Modal.Title>{this.props.title}</Modal.Title>
          </Modal.Header>
          <Modal.Body>{defaultBody}</Modal.Body>
          <Modal.Footer>
            <Button
              bsStyle="default"
              className="btn-cancel"
              onClick={this.closeEditPlanPopup}
            >
              Cancel
            </Button>
            <Button bsStyle="primary" onClick={this.submit}>
              {this.props.actionName}
            </Button>
          </Modal.Footer>
        </Modal>
      </span>
    );
  }
}

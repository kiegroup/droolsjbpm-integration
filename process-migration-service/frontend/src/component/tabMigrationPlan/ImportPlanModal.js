import React from "react";
import PropTypes from "prop-types";

import { Button, Modal } from "patternfly-react";
import Form from "patternfly-react/dist/js/components/Form/Form";
import {
  FormGroup,
  FormControl
} from "patternfly-react/dist/js/components/Form";
import ControlLabel from "patternfly-react/dist/js/components/Form/ControlLabel";
import Notification from "../Notification";
import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";

export default class ImportPlanModal extends React.Component {
  constructor() {
    super();
    this.state = { showModal: false, isValid: false, errorMsg: "" };
  }
  onCloseModal = () => {
    this.setState({ showModal: false });
  };
  onOpenModal = () => {
    this.setState({
      showModal: true,
      planObject: {},
      plan: ""
    });
  };
  onChange = event => {
    let planObject;
    try {
      planObject = JSON.parse(event.target.value);
    } catch (e) {
      planObject = {};
    }
    this.setState({
      isValid: planObject !== {},
      planObject,
      plan: event.target.value
    });
  };

  submit = () => {
    this.props
      .onImport(this.state.planObject)
      .then(() => this.onCloseModal())
      .catch(() => this.setState({ errorMsg: "Unable to import the plan" }));
  };

  render() {
    const notification = (
      <Notification
        type={ALERT_TYPE_ERROR}
        message={this.state.errorMsg}
        onDismiss={() => this.setState({ errorMsg: "" })}
      />
    );
    const formBody = (
      <Form>
        {this.state.errorMsg && notification}
        <FormGroup controlId="importText">
          <ControlLabel>Migration plan</ControlLabel>
          <FormControl
            componentClass="textarea"
            onChange={this.onChange}
            value={this.state.plan}
            rows={10}
          />
        </FormGroup>
      </Form>
    );

    return (
      <React.Fragment>
        <Button bsStyle="default" onClick={this.onOpenModal}>
          Import
        </Button>

        <Modal show={this.state.showModal} onHide={this.onCloseModal} size="lg">
          <Modal.Header>
            <Modal.CloseButton onClick={this.onCloseModal} />
            <Modal.Title>Import migration plan</Modal.Title>
          </Modal.Header>
          <Modal.Body>{formBody}</Modal.Body>
          <Modal.Footer>
            <Button
              bsStyle="default"
              className="btn-cancel"
              onClick={this.onCloseModal}
            >
              Cancel
            </Button>
            <Button
              bsStyle="primary"
              onClick={this.submit}
              disabled={!this.state.isValid}
            >
              Import
            </Button>
          </Modal.Footer>
        </Modal>
      </React.Fragment>
    );
  }
}

ImportPlanModal.propTypes = {
  onImport: PropTypes.func.isRequired
};

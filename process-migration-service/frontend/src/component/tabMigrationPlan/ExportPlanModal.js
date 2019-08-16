import React from "react";

import { Button } from "patternfly-react";
import { Modal } from "patternfly-react";
import { Icon } from "patternfly-react";
import { OverlayTrigger } from "patternfly-react";
import { Tooltip } from "patternfly-react";
import {
  Form,
  ControlLabel,
  FormGroup,
  FormControl
} from "patternfly-react/dist/js/components/Form";
export default class ExportPlanModal extends React.Component {
  constructor() {
    super();
    this.state = { showModal: false };
  }
  closeModal = () => {
    this.setState({ showModal: false });
  };
  openModal = () => {
    this.setState({ showModal: true });
  };

  copyToClipboard = () => {
    navigator.clipboard.writeText(this.planToString());
    this.setState({ showModal: false });
  };

  planToString = () => {
    return JSON.stringify(
      this.props.plan,
      (key, value) => {
        if (key !== "id" && value !== null) {
          return value;
        }
      },
      2
    );
  };

  render() {
    const tooltipExport = (
      <Tooltip id="tooltip">
        <div>Export migration plan</div>
      </Tooltip>
    );

    const formBody = (
      <Form>
        <FormGroup controlId="exportText">
          <ControlLabel>Migration plan</ControlLabel>
          <FormControl
            componentClass="textarea"
            value={this.planToString()}
            rows={10}
            readOnly
          />
        </FormGroup>
      </Form>
    );

    return (
      <React.Fragment>
        <OverlayTrigger overlay={tooltipExport} placement={"bottom"}>
          <Button bsStyle="link" onClick={this.openModal}>
            <Icon type="pf" name="export" />
          </Button>
        </OverlayTrigger>

        <Modal show={this.state.showModal} onHide={this.closeModal} size="lg">
          <Modal.Header>
            <Modal.CloseButton onClick={this.closeModal} />
            <Modal.Title>Export migration plan</Modal.Title>
          </Modal.Header>
          <Modal.Body>{formBody}</Modal.Body>
          <Modal.Footer>
            <Button
              bsStyle="default"
              className="btn-cancel"
              onClick={this.closeModal}
            >
              Cancel
            </Button>
            <Button bsStyle="primary" onClick={this.copyToClipboard}>
              Copy to clipboard
            </Button>
          </Modal.Footer>
        </Modal>
      </React.Fragment>
    );
  }
}

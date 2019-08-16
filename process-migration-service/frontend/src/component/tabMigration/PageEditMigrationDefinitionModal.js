import React from "react";
import PropTypes from "prop-types";

import { Button, Modal, Icon, OverlayTrigger, Tooltip } from "patternfly-react";

import PageMigrationScheduler from "../tabMigrationPlan/wizardExecuteMigration/PageMigrationScheduler";
import MigrationClient from "../../clients/migrationClient";
import FormGroup from "patternfly-react/dist/js/components/Form/FormGroup";
import ControlLabel from "patternfly-react/dist/js/components/Form/ControlLabel";
import FormControl from "patternfly-react/dist/js/components/Form/FormControl";
import { Form } from "patternfly-react/dist/js/components/Form";

export default class PageEditMigrationDefinitionModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      id: this.props.migrationId,
      showEditDialog: false,
      isValid: true,
      definition: {
        execution: {},
        processInstanceIds: []
      }
    };
  }

  openEditDialog = () => {
    MigrationClient.get(this.props.migrationId).then(migration => {
      this.setState({
        showEditDialog: true,
        definition: migration.definition
      });
    });
  };

  hideEditDialog = () => {
    this.setState({ showEditDialog: false });
  };

  submit = () => {
    MigrationClient.update(this.props.migrationId, this.state.definition).then(
      () => {
        this.hideEditDialog();
      }
    );
  };

  onExecutionFieldChange = (field, value) => {
    const { definition } = this.state;
    if (value === null) {
      delete definition.execution[field];
    } else {
      definition.execution[field] = value;
    }
    this.setState({ definition });
  };

  onFormIsValid = isValid => {
    this.setState({ isValid });
  };

  render() {
    const tooltipEdit = (
      <Tooltip id="tooltip">
        <div>Edit</div>
      </Tooltip>
    );

    const defaultBody = (
      <Form>
        <FormGroup controlId="EditMigration_PlanId">
          <ControlLabel>Plan ID:</ControlLabel>
          <FormControl
            type="text"
            readOnly
            value={this.state.definition.planId}
          />
        </FormGroup>
        <FormGroup controlId="EditMigration_Pids">
          <ControlLabel>Process instances ID: </ControlLabel>
          <FormControl
            type="text"
            readOnly
            value={this.state.definition.processInstanceIds.sort()}
          />
        </FormGroup>
        <FormGroup controlId="EditMigration_kieId">
          <ControlLabel>KIE Server ID: </ControlLabel>
          <FormControl
            type="text"
            readOnly
            value={this.state.definition.kieServerId}
          />
        </FormGroup>
        <PageMigrationScheduler
          callbackUrl={this.state.definition.execution.callbackUrl}
          scheduledStartTime={
            this.state.definition.execution.scheduledStartTime
          }
          onFieldChange={this.onExecutionFieldChange}
          onIsValid={this.onFormIsValid}
        />
      </Form>
    );

    return (
      <div>
        <OverlayTrigger overlay={tooltipEdit} placement={"bottom"}>
          <Button bsStyle="link" onClick={this.openEditDialog}>
            <Icon type="fa" name="edit" />
          </Button>
        </OverlayTrigger>
        <Modal
          show={this.state.showEditDialog}
          onHide={this.hideEditDialog}
          size="lg"
        >
          <Modal.Header>
            <Modal.CloseButton onClick={this.hideEditDialog} />
            <Modal.Title>Edit Migration Definition</Modal.Title>
          </Modal.Header>
          <Modal.Body>{defaultBody}</Modal.Body>
          <Modal.Footer>
            <Button
              bsStyle="default"
              className="btn-cancel"
              onClick={this.hideEditDialog}
            >
              Cancel
            </Button>
            <Button
              bsStyle="primary"
              onClick={this.submit}
              disabled={!this.state.isValid}
            >
              Submit
            </Button>
          </Modal.Footer>
        </Modal>
      </div>
    );
  }
}

PageEditMigrationDefinitionModal.propTypes = {
  migrationId: PropTypes.number.isRequired
};

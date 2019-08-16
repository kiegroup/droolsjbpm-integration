import React from "react";
import PropTypes from "prop-types";

import { ALERT_TYPE_ERROR } from "patternfly-react/dist/js/components/Alert/AlertConstants";

import Notification from "../../Notification";
import KieServerClient from "../../../clients/kieServerClient";
import ProcessSelector from "./ProcessSelector";
import { Form } from "patternfly-react/dist/js/components/Form";

export default class PageDefinition extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMsg: "",
      containers: {}
    };
    this.loadProcessIds();
    this.props.onIsValid(
      this.validate(this.props.plan.source, this.props.plan.target) === ""
    );
    if (this.validateItem(this.props.plan.source)) {
      this.getDefinition(
        this.props.plan.source,
        this.props.setSourceDefinition
      );
    }
    if (this.validateItem(this.props.plan.target)) {
      this.getDefinition(
        this.props.plan.target,
        this.props.setTargetDefinition
      );
    }
  }

  loadProcessIds = () => {
    KieServerClient.getDefinitions(this.props.kieServerId).then(containers => {
      this.setState({
        containers
      });
    });
  };

  getDefinition = (definition, callbackFn) => {
    KieServerClient.getDefinition(
      this.props.kieServerId,
      definition.containerId,
      definition.processId
    )
      .then(res => {
        callbackFn(res);
      })
      .catch(err => {
        const status = err.response.status;
        let errorMsg;
        if (status === 404) {
          if (definition.containerId !== "" || definition.processId !== "") {
            errorMsg = `Process not found: ${definition.containerId}/${definition.processId}`;
          } else {
            errorMsg = "Provide a valid containerId and processId";
          }
          this.setState({ errorMsg });
          callbackFn("");
        } else {
          err.json().then(json => {
            this.setState({
              errorMsg: `${definition.containerId}/${definition.processId}: ${json.message.string}`
            });
            callbackFn("");
          });
        }
      });
  };

  validateItem = item => {
    const isFieldValid = field => {
      return field !== undefined && field !== "";
    };
    return (
      item !== undefined &&
      isFieldValid(item.containerId) &&
      isFieldValid(item.processId)
    );
  };

  validate = (source, target) => {
    const validSrc = this.validateItem(source);
    const validTgt = this.validateItem(target);
    if (!validSrc) {
      return "Provide a valid source";
    }
    if (!validTgt) {
      return "Provide a valid target";
    }
    if (
      source.containerId === target.containerId &&
      source.processId === target.processId
    ) {
      return "Source and target must be different";
    }
    return "";
  };

  onChangeSource = item => {
    const validationError = this.validate(item, this.props.plan.target);
    const isValidUpdate = validationError === "";
    this.props.onIsValid(isValidUpdate);
    this.props.onChangeSource(item.containerId, item.processId);
    if (this.validateItem(item)) {
      this.getDefinition(item, this.props.setSourceDefinition);
    }
    this.setState({ validationError });
  };

  onChangeTarget = item => {
    const validationError = this.validate(this.props.plan.source, item);
    const isValidUpdate = validationError === "";
    this.props.onIsValid(isValidUpdate);
    this.props.onChangeTarget(item.containerId, item.processId);
    if (this.validateItem(item)) {
      this.getDefinition(item, this.props.setTargetDefinition);
    }
    this.setState({ validationError });
  };

  render() {
    const notification = (
      <Notification
        type={ALERT_TYPE_ERROR}
        message={this.state.errorMsg}
        onDismiss={() => this.setState({ errorMsg: "" })}
      />
    );
    return (
      <Form horizontal>
        {this.state.errorMsg && notification}
        <ProcessSelector
          type="Source"
          options={this.state.containers}
          containerId={this.props.plan.source.containerId}
          processId={this.props.plan.source.processId}
          onChange={this.onChangeSource}
        />
        <ProcessSelector
          type="Target"
          options={this.state.containers}
          containerId={this.props.plan.target.containerId}
          processId={this.props.plan.target.processId}
          onChange={this.onChangeTarget}
          validationError={this.state.validationError}
        />
      </Form>
    );
  }
}

PageDefinition.propTypes = {
  kieServerId: PropTypes.string.isRequired,
  plan: PropTypes.object.isRequired,
  onIsValid: PropTypes.func.isRequired,
  onChangeSource: PropTypes.func.isRequired,
  onChangeTarget: PropTypes.func.isRequired,
  setSourceDefinition: PropTypes.func.isRequired,
  setTargetDefinition: PropTypes.func.isRequired
};

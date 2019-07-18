import React from "react";
import PropTypes from "prop-types";
import {
  FormGroup,
  ControlLabel,
  Form
} from "patternfly-react/dist/js/components/Form";
import FormControl from "patternfly-react/dist/js/components/Form/FormControl";
import { Col } from "patternfly-react/dist/js/components/Grid";
import HelpBlock from "patternfly-react/dist/js/components/Form/HelpBlock";

export default class PagePlanName extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      dirty: false
    };
    this.props.onIsValid(this.isFormValid());
  }
  onDescriptionChange = event => {
    this.props.onFieldChanged(
      "description",
      event.target.value === "" ? null : event.target.value
    );
  };

  onNameChange = event => {
    this.props.onFieldChanged("name", event.target.value);
    this.props.onIsValid(this.isFormValid());
    this.setState({ dirty: true });
  };

  isFormValid = () => {
    return this.props.plan.name !== undefined && this.props.plan.name !== "";
  };

  getValidationState = () => {
    if (this.state.dirty && this.props.plan.name === "") {
      return "error";
    }
    return null;
  };

  render() {
    return (
      <Form horizontal>
        <FormGroup
          controlId="formPlanName"
          validationState={this.getValidationState()}
          className="required"
        >
          <Col componentClass={ControlLabel} sm={3}>
            Name
          </Col>
          <Col sm={9}>
            <FormControl
              type="text"
              value={this.props.plan.name ? this.props.plan.name : ""}
              onChange={this.onNameChange}
            />
            <FormControl.Feedback />
            {this.getValidationState() && (
              <HelpBlock>The name is mandatory</HelpBlock>
            )}
          </Col>
        </FormGroup>
        <FormGroup controlId="formPlanDescription">
          <Col componentClass={ControlLabel} sm={3}>
            Description
          </Col>
          <Col sm={9}>
            <FormControl
              type="text"
              value={
                this.props.plan.description ? this.props.plan.description : ""
              }
              onChange={this.onDescriptionChange}
            />
          </Col>
        </FormGroup>
      </Form>
    );
  }
}

PagePlanName.propTypes = {
  plan: PropTypes.object.isRequired,
  onFieldChanged: PropTypes.func.isRequired,
  onIsValid: PropTypes.func.isRequired
};

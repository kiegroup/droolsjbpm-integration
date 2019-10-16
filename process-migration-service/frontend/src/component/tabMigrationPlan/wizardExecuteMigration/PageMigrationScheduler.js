import React from "react";
import PropTypes from "prop-types";

import Datetime from "react-datetime";
import validator from "validator";
import moment from "moment";
import FormGroup from "patternfly-react/dist/js/components/Form/FormGroup";
import {
  ControlLabel,
  FormControl,
  HelpBlock
} from "patternfly-react/dist/js/components/Form";
import Radio from "patternfly-react/dist/js/components/Form/Radio";

export default class PageMigrationScheduler extends React.Component {
  constructor(props) {
    super(props);
    const isValidUrl = this.validateCallbackUrl(props.callbackUrl) === null;
    const isValidTime =
      this.validateScheduledStartTime(
        this.formatScheduledStartTime(props.scheduledStartTime)
      ) === null;
    this.state = {
      scheduleMigration: this.props.scheduledStartTime !== "",
      isValidUrl,
      isValidTime,
      showTimePicker: false
    };
    this.onValidationChange(isValidUrl, isValidTime);
  }

  onValidationChange = (isValidUrl, isValidTime) => {
    this.props.onIsValid(isValidUrl && isValidTime);
  };

  validateCallbackUrl = callbackUrl => {
    let validationMessageUrl = null;
    if (
      callbackUrl !== undefined &&
      callbackUrl !== "" &&
      !validator.isURL(callbackUrl, { require_tld: false })
    ) {
      validationMessageUrl = "error";
    }
    return validationMessageUrl;
  };

  validateScheduledStartTime = scheduledStartTime => {
    if (scheduledStartTime === null || moment().diff(scheduledStartTime) < 0) {
      return null;
    }
    return "error";
  };

  changeCallbackUrl = event => {
    const inputUrl = event.target.value;
    const isValidUrl = this.validateCallbackUrl(inputUrl) === null;
    this.props.onFieldChange("callbackUrl", inputUrl === "" ? null : inputUrl);
    this.setState({ isValidUrl });
    this.onValidationChange(isValidUrl, this.state.isValidTime);
  };

  handleDateTimeInput = inputMoment => {
    const validationMessageTime = this.validateScheduledStartTime(inputMoment);
    const isValidTime = validationMessageTime === null;
    if (moment(inputMoment).isValid()) {
      this.props.onFieldChange("scheduledStartTime", inputMoment.toISOString());
    }
    this.setState({ isValidTime, validationMessageTime });
    this.onValidationChange(this.state.isValidUrl, isValidTime);
  };

  isDateSelectable = current =>
    current.isAfter(Datetime.moment().subtract(1, "day"));

  hideScheduleMigration = () => {
    this.setState({
      scheduleMigration: false
    });
    this.props.onFieldChange("scheduledStartTime", null);
  };

  formatScheduledStartTime = scheduledStartTime => {
    if (scheduledStartTime === "") {
      return null;
    }
    return moment(this.props.scheduledStartTime);
  };

  render() {
    const scheduledStartTime = this.formatScheduledStartTime(
      this.props.scheduledStartTime
    );
    const callbackUrlValidation = this.validateCallbackUrl(
      this.props.callbackUrl
    );
    const scheduledStartTimeValidation = this.validateScheduledStartTime(
      scheduledStartTime
    );

    return (
      <React.Fragment>
        <FormGroup
          controlId="formMigrationScheduler_CallbackURL"
          validationState={callbackUrlValidation}
        >
          <ControlLabel>Callback URL</ControlLabel>
          <FormControl
            type="text"
            value={this.props.callbackUrl}
            onChange={this.changeCallbackUrl}
          />
          <FormControl.Feedback />
          {callbackUrlValidation && <HelpBlock>Enter a valid URL</HelpBlock>}
        </FormGroup>
        <FormGroup
          controlId="formMigrationScheduler_Schedule"
          validationState={scheduledStartTimeValidation}
        >
          <ControlLabel>Run migration</ControlLabel>
          <FormGroup controlId="formMigrationScheduler_ScheduleRadio">
            <Radio
              checked={!this.state.scheduleMigration}
              onChange={this.hideScheduleMigration}
            >
              Now
            </Radio>
            <Radio
              checked={this.state.scheduleMigration}
              onChange={() => this.setState({ scheduleMigration: true })}
            >
              Schedule
            </Radio>
          </FormGroup>
          {this.state.scheduleMigration && (
            <Datetime
              id="PageMigrationScheduler_scheduleTime"
              onChange={this.handleDateTimeInput}
              isValidDate={this.isDateSelectable}
              value={scheduledStartTime}
              input
              onFocus={() => this.setState({ showTimePicker: true })}
              onBlur={() => this.setState({ showTimePicker: false })}
              open={this.state.showTimePicker}
            />
          )}
          <FormControl.Feedback />
          {scheduledStartTimeValidation && (
            <HelpBlock>Select a valid date in the future</HelpBlock>
          )}
        </FormGroup>
      </React.Fragment>
    );
  }
}

PageMigrationScheduler.defaultProps = {
  callbackUrl: "",
  scheduledStartTime: ""
};

PageMigrationScheduler.propTypes = {
  callbackUrl: PropTypes.string,
  scheduledStartTime: PropTypes.string,
  onFieldChange: PropTypes.func.isRequired,
  onIsValid: PropTypes.func.isRequired
};

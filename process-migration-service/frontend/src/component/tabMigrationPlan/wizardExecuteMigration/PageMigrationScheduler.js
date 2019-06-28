import React from "react";
import PropTypes from "prop-types";

import Datetime from "react-datetime";
import validator from "validator";
import moment from "moment";
import FormGroup from "patternfly-react/dist/js/components/Form/FormGroup";
import {
  ControlLabel,
  FormControl
} from "patternfly-react/dist/js/components/Form";
import Radio from "patternfly-react/dist/js/components/Form/Radio";

export default class PageMigrationScheduler extends React.Component {
  constructor(props) {
    super(props);
    const isValidUrl =
      this.validateCallbackUrl(this.props.callbackUrl) === null;
    const isValidTime =
      this.validateScheduledStartTime(this.props.scheduledStartTime) === null;
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

  getUrlValidationState = () => {
    return this.validateCallbackUrl(this.props.callbackUrl);
  };

  validateCallbackUrl = callbackUrl => {
    let validationMessageUrl = null;
    if (
      callbackUrl !== undefined &&
      callbackUrl !== "" &&
      !validator.isURL(callbackUrl)
    ) {
      validationMessageUrl = "error";
    }
    return validationMessageUrl;
  };

  validateScheduledStartTime = scheduledStartTime => {
    let validationMessageTime = null;
    if (
      scheduledStartTime !== "" &&
      !moment(scheduledStartTime, "YYYY-MM-DDTHH:mm:ssZ", true).isValid()
    ) {
      validationMessageTime = "Select a valid date";
    }
    return validationMessageTime;
  };

  changeCallbackUrl = event => {
    const inputUrl = event.target.value;
    const isValidUrl = this.validateCallbackUrl(inputUrl) === null;
    this.props.onFieldChange("callbackUrl", inputUrl);
    this.setState({ isValidUrl });
    this.onValidationChange(isValidUrl, this.state.isValidTime);
  };

  handleDateTimeInput = inputMoment => {
    const isValidTime = this.validateScheduledStartTime(inputMoment) === null;
    if (isValidTime) {
      this.props.onFieldChange("scheduledStartTime", inputMoment.format());
    }
    this.setState({ isValidTime });
    this.onValidationChange(this.state.isValidUrl, isValidTime);
  };

  validDate = current => {
    var yesterday = Datetime.moment().subtract(1, "day");
    return current.isAfter(yesterday);
  };

  hideScheduleMigration = () => {
    this.setState({
      scheduleMigration: false
    });
    this.props.onFieldChange("scheduledStartTime", "");
  };

  render() {
    return (
      <React.Fragment>
        <FormGroup
          controlId="formMigrationScheduler_CallbackURL"
          validationState={this.getUrlValidationState()}
        >
          <ControlLabel>Callback URL</ControlLabel>
          <FormControl
            type="text"
            value={this.props.callbackUrl}
            onChange={this.changeCallbackUrl}
          />
          <FormControl.Feedback />
        </FormGroup>
        <FormGroup controlId="formMigrationScheduler_Schedule">
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
              isValidDate={this.validDate}
              value={this.props.scheduledStartTime}
              input
              utc
              onFocus={() => this.setState({ showTimePicker: true })}
              disableOnClickOutside
              open={this.state.showTimePicker}
            />
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

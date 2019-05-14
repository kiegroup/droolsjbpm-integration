import React, { Component } from "react";
import Datetime from "react-datetime";
import validator from "validator";
import moment from "moment";

export default class PageMigrationScheduler extends Component {
  constructor(props) {
    super(props);
    this.state = {
      dateTimeInput: true,
      validationMessageUrl: "",
      validationMessageTime: ""
    };
  }

  changeCallbackUrl = event => {
    const inputUrl = event.target.value;
    //user validator to set only the valid URL.
    if (validator.isURL(inputUrl)) {
      this.props.setCallbackUrl(inputUrl);
      this.setState({
        validationMessageUrl: ""
      });
    } else if (inputUrl == "") {
      this.setState({
        validationMessageUrl: ""
      });
    } else {
      this.props.setCallbackUrl("");
      this.setState({
        validationMessageUrl: "Error: Callback input is not a valid URL."
      });
    }
  };

  disableScheduleTime = () => {
    this.setState({
      dateTimeInput: false
    });
    this.props.setScheduleStartTime("");
  };

  enableScheduleTime = () => {
    this.setState({
      dateTimeInput: true
    });
  };

  handleDateTimeInput = inputMoment => {
    if (moment(inputMoment, "YYYY-MM-DDTHH:mm:ss", true).isValid()) {
      this.props.setScheduleStartTime(inputMoment.toDate());
      this.setState({
        validationMessageTime: ""
      });
    } else {
      this.setState({
        validationMessageTime: "Error: not valid time"
      });
    }
  };

  validDate = current => {
    var yesterday = Datetime.moment().subtract(1, "day");
    return current.isAfter(yesterday);
  };

  render() {
    return (
      <div className="form-horizontal">
        <div className="form-group">
          <label
            className="col-md-4 control-label"
            data-testid="testid_callback"
          >
            Callback URL:
          </label>
          <div className="col-md-8">
            <input
              type="text"
              name="callbackUrl"
              onChange={this.changeCallbackUrl}
            />
          </div>
        </div>
        <div className="form-group">
          <label className="col-md-4 control-label">Run migration:</label>
          <div className="col-md-8">
            <div className="radio">
              <label data-testid="testid_syncMode">
                <input
                  type="radio"
                  name="timeType"
                  value="1"
                  onClick={this.disableScheduleTime}
                />
                Now
              </label>
            </div>
            <div className="radio">
              <label data-testid="testid_asyncMode">
                <input
                  type="radio"
                  name="timeType"
                  value="2"
                  onClick={this.enableScheduleTime}
                />
                Schedule
                <Datetime
                  id="PageMigrationScheduler_scheduleTime"
                  input={this.state.dateTimeInput}
                  onChange={this.handleDateTimeInput}
                  isValidDate={this.validDate}
                />
              </label>
            </div>
          </div>
        </div>

        <div className="form-group" />

        <div className="form-group" />

        <div className="form-group" />

        <div className="form-group">
          <b>
            <div className="col-md-8">{this.state.validationMessageUrl}</div>
          </b>
        </div>
        <div className="form-group">
          <b>
            <div className="col-md-8">{this.state.validationMessageTime}</div>
          </b>
        </div>
      </div>
    );
  }
}

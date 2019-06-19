import React from "react";
import PropTypes from "prop-types";
import {
  ToastNotificationList,
  TimedToastNotification
} from "patternfly-react";

export default class Notification extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <ToastNotificationList>
        <TimedToastNotification
          type={this.props.type}
          persistent={false}
          onDismiss={this.props.onDismiss}
        >
          {this.props.message}
        </TimedToastNotification>
      </ToastNotificationList>
    );
  }
}

Notification.propTypes = {
  type: PropTypes.string.isRequired,
  message: PropTypes.string.isRequired,
  onDismiss: PropTypes.func.isRequired
};

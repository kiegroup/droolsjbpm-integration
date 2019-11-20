import React, { Component } from "react";
import PropTypes from "prop-types";
import {
  sortableHeaderCellFormatter,
  actionHeaderCellFormatter,
  Icon,
  OverlayTrigger,
  Tooltip,
  Table,
  Wizard
} from "patternfly-react";

import { MigrationLog } from "./MigrationLog";
import AdvancedTable from "../common/AdvancedTable";

const tooltipViewLog = (
  <Tooltip id="tooltip">
    <div>View Log</div>
  </Tooltip>
);

const PrimaryLogDetailsContent = props => {
  const { log } = props;

  if (log != null && log != "") {
    return (
      <Table.Actions key="1">
        <MigrationLog log={log} />
      </Table.Actions>
    );
  } else {
    return <div> No detail migration log</div>;
  }
};

const emptyValueTableCellFormatter = value =>
  value ? <Table.Cell>{value}</Table.Cell> : <Table.Cell>N/A</Table.Cell>;

export default class PageViewMigrationLogs extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isLogDetailsShown: false,
      columns: [
        {
          property: "id",
          header: {
            label: "ID",
            props: {
              index: 0,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [],
            formatters: [],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 0
            },
            formatters: [emptyValueTableCellFormatter]
          }
        },
        {
          property: "migrationId",
          header: {
            label: "Migration ID",
            props: {
              index: 1,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [],
            formatters: [],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 1
            },
            formatters: [emptyValueTableCellFormatter]
          }
        },
        {
          property: "processInstanceId",
          header: {
            label: "Process Instance ID",
            props: {
              index: 2,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [],
            formatters: [],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 2
            },
            formatters: [emptyValueTableCellFormatter]
          }
        },
        {
          property: "startDate",
          header: {
            label: "Started Time",
            props: {
              index: 3,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [],
            formatters: [],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 3
            },
            formatters: [emptyValueTableCellFormatter]
          }
        },
        {
          property: "endDate",
          header: {
            label: "Finished Time",
            props: {
              index: 4,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [],
            formatters: [],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 4
            },
            formatters: [emptyValueTableCellFormatter]
          }
        },
        {
          property: "successful",
          header: {
            label: "Status",
            props: {
              index: 5,
              rowSpan: 1,
              colSpan: 1
            },
            transforms: [],
            formatters: [],
            customFormatters: [sortableHeaderCellFormatter]
          },
          cell: {
            props: {
              index: 5
            },
            formatters: [
              (value, { rowData }) => {
                if (rowData.successful) {
                  return (
                    <Table.Cell
                      key={
                        rowData.migrationId + "_" + rowData.id + "_" + "SUCCESS"
                      }
                      className="success"
                    >
                      SUCCESS
                    </Table.Cell>
                  );
                } else {
                  return (
                    <Table.Cell
                      key={
                        rowData.migrationId + "_" + rowData.id + "_" + "FAILED"
                      }
                      className="danger"
                    >
                      FAILED
                    </Table.Cell>
                  );
                }
              }
            ]
          }
        },
        {
          header: {
            label: "Actions",
            props: {
              index: 6,
              rowSpan: 1,
              colSpan: 1
            },
            formatters: [actionHeaderCellFormatter]
          },
          cell: {
            formatters: [
              (value, { rowData }) => [
                <Table.Actions key="0">
                  <OverlayTrigger overlay={tooltipViewLog} placement={"bottom"}>
                    <Table.Button
                      bsStyle="link"
                      onClick={() => this.showLogDetails(rowData)}
                    >
                      <Icon type="fa" name="info-circle" />
                    </Table.Button>
                  </OverlayTrigger>
                </Table.Actions>
              ]
            ]
          },
          property: "logs"
        }
      ],
      currentLogData: {}
    };
  }

  showLogDetails = log => {
    this.setState({ isLogDetailsShown: true, currentLogData: log });
  };

  hideLogDetails = () => {
    this.setState({ isLogDetailsShown: false });
  };

  render() {
    const { migrationLogs } = this.props;
    const { isLogDetailsShown, columns, currentLogData } = this.state;

    if (migrationLogs != null && migrationLogs != "") {
      return (
        <div>
          <Wizard
            show={isLogDetailsShown}
            onHide={this.hideLogDetails}
            id="logDetailWizard"
          >
            <Wizard.Header
              onClose={this.hideLogDetails}
              title="Migration Log Detail"
            />
            <Wizard.Body>
              <Wizard.Row>
                <Wizard.Main>
                  <Wizard.Contents stepIndex={0} activeStepIndex={0}>
                    <PrimaryLogDetailsContent log={currentLogData} />
                  </Wizard.Contents>
                </Wizard.Main>
              </Wizard.Row>
            </Wizard.Body>
          </Wizard>
          <AdvancedTable rows={migrationLogs} columns={columns} isSortable />
        </div>
      );
    } else {
      return <div> No detail migration logs for this status</div>;
    }
  }
}

PageViewMigrationLogs.propTypes = {
  migrationLogs: PropTypes.arrayOf(PropTypes.object)
};

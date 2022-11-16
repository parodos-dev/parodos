/*
 *  Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * @author Richard Wang (Github: RichardW98)
 */

/*
 *  Copyright (c) 2022 Red Hat Developer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * @author Richard Wang (Github: RichardW98)
 */

import '@patternfly/react-core/dist/styles/base.css';
import './fonts.css';

import React, { useContext } from 'react';
import { LogViewer, LogViewerSearch } from '@patternfly/react-log-viewer';
import {
  Button,
  Toolbar,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
  ToolbarToggleGroup,
  Tooltip,
} from '@patternfly/react-core';
import OutlinedPlayCircleIcon from '@patternfly/react-icons/dist/esm/icons/outlined-play-circle-icon';
import ExpandIcon from '@patternfly/react-icons/dist/esm/icons/expand-icon';
import PauseIcon from '@patternfly/react-icons/dist/esm/icons/pause-icon';
import PlayIcon from '@patternfly/react-icons/dist/esm/icons/play-icon';
import EllipsisVIcon from '@patternfly/react-icons/dist/esm/icons/ellipsis-v-icon';
import DownloadIcon from '@patternfly/react-icons/dist/esm/icons/download-icon';
import { RunDetailContext } from '../../../contexts/projects/runDetail';
import useGetPipelineLog from '../../../hooks/useGetPipelineLog';
import { Box } from '@material-ui/core';

const PipelineLogViewer = projectId => {
  const runDetailContext = useContext(RunDetailContext);
  const [isPaused, setIsPaused] = React.useState(false);
  const [isFullScreen, setIsFullScreen] = React.useState(false);
  const [itemCount, setItemCount] = React.useState(1);
  const [currentItemCount, setCurrentItemCount] = React.useState(0);
  const [renderData, setRenderData] = React.useState('');
  const useGetPipelineLogHook = useGetPipelineLog();
  const [timer, setTimer] = React.useState(0);
  const [selectedData, setSelectedData] = React.useState('');
  //   data.data4.split('\n'),
  // );
  const [buffer, setBuffer] = React.useState([]);
  const [linesBehind, setLinesBehind] = React.useState(0);
  const logViewerRef = React.useRef();

  React.useEffect(() => {
    setTimer(
      window.setInterval(() => {
        useGetPipelineLogHook
          .getLog(projectId, runDetailContext.selectedBranch)
          .then(log => {
            const data = log?.split('\n');
            setSelectedData(data);
            setItemCount(data.length - 1);
          });
      }, 500),
    );
    return () => {
      window.clearInterval(timer);
    };
  }, []);

  React.useEffect(() => {
    if (itemCount > selectedData.length) {
      window.clearInterval(timer);
    } else {
      setBuffer(selectedData.slice(0, itemCount));
    }
    console.log('buffer:', buffer);
  }, [itemCount]);

  React.useEffect(() => {
    if (!isPaused && buffer.length > 0) {
      setCurrentItemCount(buffer.length);
      setRenderData(buffer.join('\n'));
      if (logViewerRef && logViewerRef.current) {
        logViewerRef.current.scrollToBottom();
      }
    } else if (buffer.length !== currentItemCount) {
      setLinesBehind(buffer.length - currentItemCount);
    } else {
      setLinesBehind(0);
    }
  }, [isPaused, buffer]);

  const onExpandClick = event => {
    const element = document.querySelector('#pipeline-log-viewer');

    if (!isFullScreen) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen();
      } else if (element.webkitRequestFullScreen) {
        element.webkitRequestFullScreen(Element.ALLOW_KEYBOARD_INPUT);
      }
      setIsFullScreen(true);
    } else {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        /* Safari */
        document.webkitExitFullscreen();
      } else if (document.msExitFullscreen) {
        /* IE11 */
        document.msExitFullscreen();
      }
      setIsFullScreen(false);
    }
  };

  const onDownloadClick = () => {
    const element = document.createElement('a');
    const dataToDownload = [selectedData];
    const file = new Blob(dataToDownload, { type: 'text/plain' });
    element.href = URL.createObjectURL(file);
    element.download = `log.txt`;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  };

  const onScroll = ({
    scrollOffsetToBottom,
    scrollDirection,
    scrollUpdateWasRequested,
  }) => {
    if (!scrollUpdateWasRequested) {
      if (scrollOffsetToBottom > 0) {
        setIsPaused(true);
      } else {
        setIsPaused(false);
      }
    }
  };

  const ControlButton = () => {
    return (
      <Button
        variant={isPaused ? 'plain' : 'link'}
        onClick={() => {
          setIsPaused(!isPaused);
        }}
      >
        {isPaused ? <PlayIcon /> : <PauseIcon />}
        {isPaused ? ` Resume Log` : ` Pause Log`}
      </Button>
    );
  };

  const leftAlignedToolbarGroup = (
    <React.Fragment>
      <ToolbarToggleGroup toggleIcon={<EllipsisVIcon />} breakpoint="md">
        <ToolbarItem variant="search-filter">
          <LogViewerSearch
            onFocus={e => setIsPaused(true)}
            placeholder="Search"
            minSearchChars={0}
          />
        </ToolbarItem>
      </ToolbarToggleGroup>
      <ToolbarItem>
        <ControlButton />
      </ToolbarItem>
    </React.Fragment>
  );

  const rightAlignedToolbarGroup = (
    <React.Fragment>
      <ToolbarGroup variant="icon-button-group">
        <ToolbarItem>
          <Tooltip position="top" content={<div>Download</div>}>
            <Button
              onClick={onDownloadClick}
              variant="plain"
              aria-label="Download current logs"
            >
              <DownloadIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
        <ToolbarItem>
          <Tooltip position="top" content={<div>Expand</div>}>
            <Button
              onClick={onExpandClick}
              variant="plain"
              aria-label="View log viewer in full screen"
            >
              <ExpandIcon />
            </Button>
          </Tooltip>
        </ToolbarItem>
      </ToolbarGroup>
    </React.Fragment>
  );

  const FooterButton = () => {
    const handleClick = e => {
      setIsPaused(false);
    };
    return (
      <Button onClick={handleClick} isBlock>
        <OutlinedPlayCircleIcon />
        resume {linesBehind === 0 ? null : `and show ${linesBehind} lines`}
      </Button>
    );
  };
  return (
    <Box style={{ backgroundColor: 'white' }}>
      <LogViewer
        data={selectedData}
        id="pipeline-log-viewer"
        // scrollToRow={currentItemCount}
        ref={logViewerRef}
        height={isFullScreen ? '100%' : 250}
        toolbar={
          <Toolbar>
            <ToolbarContent>
              <ToolbarGroup alignment={{ default: 'alignLeft' }}>
                {leftAlignedToolbarGroup}
              </ToolbarGroup>
              <ToolbarGroup alignment={{ default: 'alignRight' }}>
                {rightAlignedToolbarGroup}
              </ToolbarGroup>
            </ToolbarContent>
          </Toolbar>
        }
        overScanCount={10}
        footer={isPaused && <FooterButton />}
        onScroll={onScroll}
        theme={'dark'}
      />
    </Box>
  );
};

export default PipelineLogViewer;

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

import React, { useContext } from 'react';
import CloseIcon from '@material-ui/icons/Close';

import { useStyles } from './styles';
import {
  Dialog,
  DialogContent,
  DialogTitle,
  IconButton,
  Typography,
} from '@material-ui/core';
import { RunDetailContext } from '../../../../contexts/projects/runDetail';

// interface ISimpleDialog {
// 	children?: any;
// 	open: boolean;
// 	title: any;
// 	handleClose: () => void;
// }

export default function SimpleDialog({ open, children, handleClose, title }) {
  const classes = useStyles();
  const runDetailContext = useContext(RunDetailContext);
  return (
    <Dialog
      onClose={handleClose}
      aria-labelledby="simple-dialog-title"
      open={open}
      maxWidth="lg"
      classes={{
        paper: classes.root,
      }}
    >
      <DialogTitle variant="h5" id="simple-dialog-title ">
        <Typography>{title}</Typography>
        <IconButton
          aria-label="close"
          className={classes.closeButton}
          onClick={handleClose}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent className={classes.content}>{children}</DialogContent>
    </Dialog>
  );
}

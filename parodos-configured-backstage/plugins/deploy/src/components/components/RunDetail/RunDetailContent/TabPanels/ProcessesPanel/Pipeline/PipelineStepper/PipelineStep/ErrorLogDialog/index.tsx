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

import React, { useContext, useEffect, useState } from 'react';
import {
    Box,
    Button,
    Dialog,
    DialogContent,
    DialogContentText,
    DialogTitle,
    Grid,
    IconButton,
    Typography,
} from '@material-ui/core';
import { RunDetailContext } from '../../../../../../../../../contexts/projects/runDetail';
import CloseIcon from '@material-ui/icons/Close';
import { useStyles } from '../../../../../../../../common/dialog/SimpleDialog/styles';
import useGetErrorLog from '../../../../../../../../../hooks/useGetErrorLog';

export default function ErrorLogDialog({ handleClose, stageName }) {
    const runDetailContext = useContext(RunDetailContext);
    const classes = useStyles();
    const [errorLog, setErrorLog] = useState('');
    const getErrorlog = useGetErrorLog();
    const descriptionElementRef = React.useRef(null);
    useEffect(() => {
        if (runDetailContext.openErrorLog) {
            getErrorlog
                .getLogError(
                    runDetailContext.projectId(),
                    runDetailContext.selectedBranch(),
                )
                .then(errLog => setErrorLog(errLog));
            const { current: descriptionElement } = descriptionElementRef;
            if (descriptionElement !== null) {
                descriptionElement.focus();
            }
        }
    }, [runDetailContext.openErrorLog]);

    return (
        <Dialog
            open={runDetailContext.openErrorLog}
            onClose={handleClose}
            scroll={'paper'}
            aria-labelledby="scroll-dialog-title"
            aria-describedby="scroll-dialog-description"
            maxWidth="md"
        >
            <DialogTitle id="scroll-dialog-title">
                <div>
                    <Typography
                        variant={'h5'}
                    >{`Error log for ${stageName.toUpperCase()}`}</Typography>
                </div>
                <IconButton
                    aria-label="close"
                    className={classes.closeButton}
                    onClick={handleClose}
                >
                    <CloseIcon />
                </IconButton>
            </DialogTitle>
            <DialogContent dividers={true}>
                <DialogContentText
                    id="scroll-dialog-description"
                    ref={descriptionElementRef}
                    tabIndex={-1}
                    style={{ whiteSpace: 'pre-line' }}
                >
                    {errorLog}
                </DialogContentText>
            </DialogContent>
            <Box m={3}>
                <Grid item container display={'flex'} alignItems="center">
                    <Button
                        onClick={handleClose}
                        variant={'contained'}
                        color={'secondary'}
                    >
                        <Typography variant={'body2'} color={'white'}>
                            CLOSE
                        </Typography>
                    </Button>
                </Grid>
            </Box>
        </Dialog>
    );
}

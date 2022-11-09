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

export interface IPipeline {
  id: string;
  label: string;
  branch: string;
  LastestCommitId: string;
  LastestCommitDetail: string;
  LastestCommitUrl: string;
  pipelineState: IPipelineState;
  pipelineStages: IPipelineStage[];
  health: IHealth;
  timeline: ITimeline[];
}

export interface IPipelineState {
  activeStageName: string;
  activeStageState: 'active' | 'inactive' | 'completed' | 'error';
}
export interface IPipelineStage {
  stageName: string;
  subStages: ISubStage[];
  state: 'active' | 'inactive' | 'completed' | 'error';
  rundetailUrl?: string;
}
export interface ISubStage {
  stageName: string;
  state: 'active' | 'inactive' | 'completed' | 'error';
}
export interface IHealth {
  size: {
    label: string;
    details: {
      lineOfCode: IHealthDetailedInfo;
      numberOfClasses: IHealthDetailedInfo;
      duplicatedBlocks: IHealthDetailedInfo;
    };
  };
  testCoverage: {
    label: string;
    details: { coverage: IHealthDetailedInfo };
  };
  security: {
    label: string;
    details: {
      vulnerabilities: IHealthDetailedInfo;
      bugs: IHealthDetailedInfo;
    };
  };
  maintain: {
    label: string;
    details: {
      codeSmells: IHealthDetailedInfo;
      complexitiy: IHealthDetailedInfo;
    };
  };
}

export interface IHealthDetailedInfo {
  transition: string;
  number: string;
  label: string;
  status: {
    url?: string;
    statusLabel: 'danger' | 'ok';
  };
}

export interface ITimeline {
  date: string;
  status: 'failed' | 'alert' | 'success';
  label: string;
  user?: string;
  url?: string;
  gitMsg?: string;
}

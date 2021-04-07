import { Feature } from '../../shared/generated';

export const formStateKey = 'form';

export interface SelectedCopyAttribute {
  objectGuid: string;
  attributeKey: string;
}

export interface FormState {
  features: Feature[];
  feature: Feature;
  closeAfterSave: boolean;
  alreadyDirty: boolean;
  formOpen: boolean;
  treeOpen: boolean;
  editing: boolean;
  multiFormWorkflow: boolean;

  copyFeature: Feature;
  copyDestinationFeatures: Feature[];
  copySelectedAttributes: SelectedCopyAttribute[];
  copySelectedFeature: string;
  copyFormOpen: boolean;
  copyOptionsOpen: boolean;
}

export const initialFormState: FormState = {
  feature: null,
  features: [],
  closeAfterSave: false,
  formOpen: false,
  alreadyDirty: false,
  treeOpen: false,
  editing: false,
  multiFormWorkflow: false,

  copyFeature: null,
  copyDestinationFeatures: null,
  copySelectedAttributes: [],
  copySelectedFeature: null,
  copyFormOpen: false,
  copyOptionsOpen: false,
};

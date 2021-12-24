import { Feature } from '../../shared/generated';
import { FormRelationModel } from './form-relation.model';

export const formStateKey = 'form';

export interface SelectedCopyAttribute {
  fid: string;
  attributeKey: string;
}

export interface FormState {
  features: Feature[];
  feature: Feature;
  closeAfterSave: boolean;
  alreadyDirty: boolean;
  formEnabled: boolean;
  formVisible: boolean;
  treeVisible: boolean;
  editing: boolean;
  multiFormWorkflow: boolean;
  bulkEditFilter?: string;
  bulkEditFeatureTypeName?: string;

  copyFeature: Feature;
  copyDestinationFeatures: Feature[];
  copySelectedAttributes: SelectedCopyAttribute[];
  copySelectedFeature: Feature;
  copyFormOpen: boolean;
  copyOptionsOpen: boolean;

  relationsFormOpen: boolean;
  allowedRelationSelectionFeatureTypes: string[];
  currentlySelectedRelatedFeature: Feature | null;
}

export const initialFormState: FormState = {
  feature: null,
  features: [],
  closeAfterSave: false,
  formEnabled: false,
  formVisible: false,
  alreadyDirty: false,
  treeVisible: false,
  editing: false,
  multiFormWorkflow: false,

  copyFeature: null,
  copyDestinationFeatures: [],
  copySelectedAttributes: [],
  copySelectedFeature: null,
  copyFormOpen: false,
  copyOptionsOpen: false,

  relationsFormOpen: false,
  allowedRelationSelectionFeatureTypes: [],
  currentlySelectedRelatedFeature: null,
};

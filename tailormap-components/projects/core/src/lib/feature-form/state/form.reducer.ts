import { Action, createReducer, on } from '@ngrx/store';
import { FormState, initialFormState } from './form.state';
import * as FormActions from './form.actions';
import { addOrUpdateFeature, removeFeature } from './form.state-helpers';

const onCloseFeatureForm = (state: FormState): FormState => ({
  ...state,
  features: [],
  feature: null,
  formEnabled: false,
  formVisible: false,
  treeVisible: false,
  multiFormWorkflow: false,
  bulkEditFilter: undefined,
  bulkEditFeatureTypeName: undefined,
});

const onSetHideFeatureForm = (state: FormState, payload: ReturnType<typeof FormActions.toggleFeatureFormVisibility>): FormState => ({
  ...state,
  formVisible: payload.visible,
});

const onSetFeature = (state: FormState, payload: ReturnType<typeof FormActions.setFeature>): FormState => ({
  ...state,
  feature: payload.feature,
  features: payload.updateFeatures
    ? addOrUpdateFeature([...state.features], payload.feature, null)
    : state.features,
});

const onSetNewFeature = (state: FormState, payload: ReturnType<typeof FormActions.setNewFeature>): FormState => {
  return {
    ...state,
    features: addOrUpdateFeature([...state.features], payload.newFeature, payload.parentId),
    feature: payload.newFeature,
    editing: false,
  };
};

const onSetOpenFeatureForm = (state: FormState, payload: ReturnType<typeof FormActions.setOpenFeatureForm>): FormState => ({
  ...state,
  features: payload.features,
  formEnabled: true,
  formVisible: true,
  closeAfterSave: typeof payload.closeAfterSave !== 'undefined' ? payload.closeAfterSave : false,
  alreadyDirty: typeof payload.alreadyDirty !== 'undefined' ? payload.alreadyDirty : false,
  editing: typeof payload.editMode !== 'undefined' ? payload.editMode : false,
  multiFormWorkflow: typeof payload.multiFormWorkflow !== 'undefined' ? payload.multiFormWorkflow : false,
  bulkEditFilter: payload.bulkEditFilter,
  bulkEditFilterType: payload.bulkEditFilterType,
  bulkEditFeatureTypeName: payload.bulkEditFeatureTypeName,
});

const onSetTreeOpen = (state: FormState, payload: ReturnType<typeof FormActions.setTreeOpen>): FormState => ({
  ...state,
  treeVisible: payload.treeOpen,
});

const onSetFormEditing = (state: FormState, payload: ReturnType<typeof FormActions.setFormEditing>): FormState => ({
  ...state,
  editing: payload.editing,
});

const onSetFeatureRemoved = (state: FormState, payload: ReturnType<typeof FormActions.setFeatureRemoved>): FormState => {
  const features = removeFeature([...state.features], payload.feature);
  const hasFeaturesLeft = payload.keepFormOpen || features.length > 0;
  return {
    ...state,
    features,
    feature: hasFeaturesLeft ? features[0] : null,
    formEnabled: hasFeaturesLeft,
    formVisible: hasFeaturesLeft,
  };
};

const onOpenCopyForm = (state: FormState, payload: ReturnType<typeof FormActions.openCopyForm>): FormState => ({
  ...state,
  copyFeature: payload.feature,
  copyFormOpen: true,
  copySelectedFeature: payload.feature,
  copyOptionsOpen: true,
});

const onSetCopySelectedFeature = (state: FormState, payload: ReturnType<typeof FormActions.setCopySelectedFeature>): FormState => ({
  ...state,
  copySelectedFeature: payload.feature,
});

const onToggleCopyDestinationFeature = (state: FormState, payload: ReturnType<typeof FormActions.toggleCopyDestinationFeature>): FormState => {
  const idx = state.copyDestinationFeatures.findIndex(f => f.fid === payload.destinationFeature.fid);
  if (idx !== -1) {
    return {
      ...state,
      copyDestinationFeatures: [
        ...state.copyDestinationFeatures.slice(0, idx),
        ...state.copyDestinationFeatures.slice(idx + 1),
      ],
    };
  }
  return {
    ...state,
    copyDestinationFeatures: [
      ...state.copyDestinationFeatures,
      payload.destinationFeature,
    ],
  };
};

const onToggleSelectedAttribute = (state: FormState, payload: ReturnType<typeof FormActions.toggleSelectedAttribute>): FormState => {
  const idx = state.copySelectedAttributes.findIndex(f => f.fid === payload.attribute.fid && f.attributeKey === payload.attribute.attributeKey);
  if (idx !== -1) {
    return {
      ...state,
      copySelectedAttributes: [
        ...state.copySelectedAttributes.slice(0, idx),
        ...state.copySelectedAttributes.slice(idx + 1),
      ],
    };
  }
  return {
    ...state,
    copySelectedAttributes: [
      ...state.copySelectedAttributes,
      payload.attribute,
    ],
  };
};

const onCloseCopyForm = (state: FormState): FormState => ({
  ...state,
  copyFormOpen: false,
  copyFeature: null,
  copySelectedFeature: null,
  copyDestinationFeatures: [],
});

const onSetCopyOptionsOpen = (state: FormState, payload: ReturnType<typeof FormActions.setCopyOptionsOpen>): FormState => ({
  ...state,
  copyOptionsOpen: payload.open,
});

const onOpenRelationsForm = (state: FormState): FormState => ({ ...state, relationsFormOpen: true });
const onCloseRelationsForm = (state: FormState): FormState => ({
  ...state,
  relationsFormOpen: false,
  allowedRelationSelectionFeatureTypes: [],
  highlightNetworkFeatures: [],
  currentlySelectedRelatedFeature: null,
});
const onAllowRelationSelection = (state: FormState, payload: ReturnType<typeof FormActions.allowRelationSelection>): FormState => ({
  ...state,
  allowedRelationSelectionFeatureTypes: payload.allowedFeatureTypes,
  currentlySelectedRelatedFeature: null,
});
const onSetCurrentlySelectedRelatedFeature = (state: FormState, payload: ReturnType<typeof FormActions.setCurrentlySelectedRelatedFeature>): FormState => ({
  ...state,
  currentlySelectedRelatedFeature: payload.relatedFeature,
});

const onToggleNetworkHighlightFeature = (state: FormState, payload: ReturnType<typeof FormActions.toggleNetworkHighlightFeature>): FormState => {
  const idx = state.highlightNetworkFeatures.findIndex(f => f.fid === payload.fid);
  if (idx === -1) {
    return {
      ...state,
      highlightNetworkFeatures: [
        ...state.highlightNetworkFeatures,
        payload,
      ],
    };
  }
  return {
    ...state,
    highlightNetworkFeatures: [
      ...state.highlightNetworkFeatures.slice(0, idx),
      ...state.highlightNetworkFeatures.slice(idx + 1),
    ],
  };
};

const onRemoveNetworkHighlightFeature = (state: FormState, payload: ReturnType<typeof FormActions.removeNetworkHighlightFeature>): FormState => {
  const idx = state.highlightNetworkFeatures.findIndex(f => f.fid === payload.fid);
  if (idx === -1) {
    return state;
  }
  return {
    ...state,
    highlightNetworkFeatures: [
      ...state.highlightNetworkFeatures.slice(0, idx),
      ...state.highlightNetworkFeatures.slice(idx + 1),
    ],
  };
};

const onClearNetworkHighlight = (state: FormState): FormState => {
  return { ...state, highlightNetworkFeatures: [] };
};

const formReducerImpl = createReducer(
  initialFormState,
  on(FormActions.setTreeOpen, onSetTreeOpen),
  on(FormActions.setFormEditing, onSetFormEditing),
  on(FormActions.setFeature, onSetFeature),
  on(FormActions.setFeatureRemoved, onSetFeatureRemoved),
  on(FormActions.setNewFeature, onSetNewFeature),
  on(FormActions.setOpenFeatureForm, onSetOpenFeatureForm),
  on(FormActions.setCloseFeatureForm, onCloseFeatureForm),
  on(FormActions.toggleFeatureFormVisibility, onSetHideFeatureForm),
  on(FormActions.openCopyForm, onOpenCopyForm),
  on(FormActions.setCopySelectedFeature, onSetCopySelectedFeature),
  on(FormActions.toggleCopyDestinationFeature, onToggleCopyDestinationFeature),
  on(FormActions.toggleSelectedAttribute, onToggleSelectedAttribute),
  on(FormActions.closeCopyForm, onCloseCopyForm),
  on(FormActions.setCopyOptionsOpen, onSetCopyOptionsOpen),
  on(FormActions.openRelationsForm, onOpenRelationsForm),
  on(FormActions.closeRelationsForm, onCloseRelationsForm),
  on(FormActions.allowRelationSelection, onAllowRelationSelection),
  on(FormActions.setCurrentlySelectedRelatedFeature, onSetCurrentlySelectedRelatedFeature),
  on(FormActions.toggleNetworkHighlightFeature, onToggleNetworkHighlightFeature),
  on(FormActions.removeNetworkHighlightFeature, onRemoveNetworkHighlightFeature),
  on(FormActions.clearNetworkHighlight, onClearNetworkHighlight),
);

export const formReducer = (state: FormState | undefined, action: Action) => {
  return formReducerImpl(state, action);
};

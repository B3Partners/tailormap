import { Action, createReducer, on } from '@ngrx/store';
import { FormState, initialFormState } from './form.state';
import * as FormActions from './form.actions';
import { Feature } from '../../shared/generated';

const onCloseFeatureForm  = (state: FormState): FormState => ({
  ...state,
  features: [],
  feature: null,
  formOpen: false,
});

const onSetFeature = (state: FormState, payload : { feature: Feature }): FormState => ({
  ...state,
  feature: payload.feature,
});

const onSetOpenFeatureForm = (state: FormState, payload: { features?: Feature[], closeAfterSave?: boolean,
                                                           alreadyDirty?: boolean }): FormState => ({
  ...state,
  features: payload.features,
  formOpen: true,
  closeAfterSave: payload.closeAfterSave || false,
  alreadyDirty: payload.alreadyDirty || false,
});

const formReducerImpl = createReducer(
  initialFormState,
  on(FormActions.setFeature, onSetFeature),
  on(FormActions.setOpenFeatureForm, onSetOpenFeatureForm),
  on(FormActions.setCloseFeatureForm, onCloseFeatureForm),
);

export const formReducer = (state: FormState | undefined, action: Action) => {
  return formReducerImpl(state, action);
}

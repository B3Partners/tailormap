import { FormCopyComponent } from './form-copy.component';
import { createComponentFactory, Spectator } from '@ngneat/spectator';
import { FormConfigMockModule } from '../../shared/formconfig-repository/formconfig-mock.module.spec';
import { SharedModule } from '../../shared/shared.module';
import { getFormActionsServiceMockProvider } from '../form-actions/form-actions.service.mock';
import { getFeatureInitializerServiceMockProvider } from '../../shared/feature-initializer/feature-initializer.service.mock';
import { FormCopyService } from './form-copy.service';
import { mockFeature } from '../../shared/tests/test-data';
import { CopyDialogData } from './form-copy-models';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { FormState, formStateKey, initialFormState } from '../state/form.state';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { applicationStateKey, initialApplicationState } from '../../application/state/application.state';
import { testFormConfigs } from '../../application/state/test-data/application-test-data';
import { ExtendedFormConfigurationModel } from '../../application/models/extended-form-configuration.model';


describe('FormCopyComponent', () => {
  let spectator: Spectator<FormCopyComponent>;
  const mockDialogData: CopyDialogData = {
    originalFeature: mockFeature(),
    destinationFeatures: [ mockFeature(), mockFeature() ],
  };
  const selectedFeature = mockFeature();
  const formState: FormState = {
    ...initialFormState,
    copyFeature: selectedFeature,
    copySelectedFeature: selectedFeature,
  };
  const initialState = {
    [formStateKey]: formState,
    [applicationStateKey]: {
      ...initialApplicationState,
      formConfigs: testFormConfigs.map<ExtendedFormConfigurationModel>(c => ({ ...c, tableName: selectedFeature.clazz })),
    },
  };
  let store: MockStore;
  const createComponent = createComponentFactory({
    component: FormCopyComponent,
    imports: [
      SharedModule,
      FormConfigMockModule,
    ],
    providers: [
      provideMockStore({ initialState }),
      getFormActionsServiceMockProvider(),
      getFeatureInitializerServiceMockProvider(),
      FormCopyService,
    ],
    schemas: [ CUSTOM_ELEMENTS_SCHEMA ],
  });

  beforeEach(() => {
    spectator = createComponent();
    store = spectator.inject(MockStore);
  });

  it('should create', () => {
    expect(spectator).toBeTruthy();
  });

});

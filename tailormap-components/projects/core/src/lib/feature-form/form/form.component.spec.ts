import { FormComponent } from './form.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { SharedModule } from '../../shared/shared.module';
import { FormCreatorComponent } from '../form-creator/form-creator.component';
import { FormTreeComponent } from '../form-tree/form-tree.component';
import { FormfieldComponent } from '../form-field/formfield.component';
import { FeatureControllerService } from '../../shared/generated';
import { FormConfigMockModule } from '../../shared/formconfig-repository/formconfig-mock.module.spec';
import { createComponentFactory, Spectator } from '@ngneat/spectator';
import { getDialogRefMockProvider } from '../../shared/tests/test-mocks';
import { getMetadataServiceMockProvider } from '../../application/services/mocks/metadata.service.mock';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { formStateKey, initialFormState } from '../state/form.state';
import { applicationStateKey, initialApplicationState } from '../../application/state/application.state';
import { getEditFeatureGeometryServiceProvider } from '../services/mocks/edit-feature-geometry.service.mock';
import { getTailorMapServiceMockProvider } from '../../../../../bridge/src/tailor-map.service.mock';
import { MatIconTestingModule } from '@angular/material/icon/testing';

describe('FormComponent', () => {
  let spectator: Spectator<FormComponent>;
  const initialState = {
    [formStateKey]: initialFormState,
    [applicationStateKey]: initialApplicationState,
  };
  let store: MockStore;

  const createComponent = createComponentFactory({
    component: FormComponent,
    imports: [
      FormsModule,
      ReactiveFormsModule,
      MatSnackBarModule,
      SharedModule,
      FormConfigMockModule,
      MatIconTestingModule,
    ],
    declarations: [
      FormCreatorComponent,
      FormTreeComponent,
      FormfieldComponent,
      FormCreatorComponent,
      FormComponent,
    ],
    providers: [
      provideMockStore({ initialState }),
      FeatureControllerService,
      getDialogRefMockProvider(),
      getMetadataServiceMockProvider(),
      getEditFeatureGeometryServiceProvider(),
      getTailorMapServiceMockProvider(),
    ],
  });

  beforeEach(() => {
    spectator = createComponent();
    store = spectator.inject(MockStore);
  });

  it('should create', () => {
    expect(spectator).toBeTruthy();
  });
});

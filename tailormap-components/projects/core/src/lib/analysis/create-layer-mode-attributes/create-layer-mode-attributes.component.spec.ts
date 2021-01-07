import { CreateLayerModeAttributesComponent } from './create-layer-mode-attributes.component';
import { createComponentFactory, Spectator } from '@ngneat/spectator';
import { analysisStateKey, initialAnalysisState } from '../state/analysis.state';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { SharedModule } from '../../shared/shared.module';
import { IdService } from '../../shared/id-service/id.service';

describe('CreateLayerModeAttributesComponent', () => {
  let spectator: Spectator<CreateLayerModeAttributesComponent>;
  const initialState = { [analysisStateKey]: initialAnalysisState };
  let store: MockStore;

  const createComponent = createComponentFactory({
    component: CreateLayerModeAttributesComponent,
    imports: [ SharedModule ],
    providers: [
      provideMockStore({ initialState }),
      IdService,
    ]
  });

  beforeEach(() => {
    spectator = createComponent();
    store = spectator.inject(MockStore);
  });

  it('should create', () => {
    expect(spectator).toBeTruthy();
  });
});

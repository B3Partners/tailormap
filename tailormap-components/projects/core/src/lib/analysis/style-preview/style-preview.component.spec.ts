import { createComponentFactory, Spectator } from '@ngneat/spectator';

import { StylePreviewComponent } from './style-preview.component';
import { analysisStateKey, initialAnalysisState } from '../state/analysis.state';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { SharedModule } from '../../shared/shared.module';
import { selectSelectedDataSource, selectStyles } from '../state/analysis.selectors';
import { getDummySelectedDataSource, getDummyUserLayerStyle } from '../helpers/test-data/style-test-data';
import { AttributeTypeEnum } from '../../application/models/attribute-type.enum';

const styles = [
  getDummyUserLayerStyle({ label: 'Boom' }),
  getDummyUserLayerStyle({ label: 'Mooie boom', fillColor: 'rgb(0, 255, 0)', strokeWidth: 20, marker: "arrow", markerSize: 18 }),
];

describe('StylePreviewComponent', () => {
  let spectator: Spectator<StylePreviewComponent>;
  const initialState = { [analysisStateKey]: initialAnalysisState };
  let store: MockStore;

  const createComponent = createComponentFactory({
    component: StylePreviewComponent,
    imports: [ SharedModule ],
    providers: [
      provideMockStore({ initialState }),
    ],
  });

  beforeEach(() => {
    spectator = createComponent();
    store = spectator.inject(MockStore);
  })

  it('should create', () => {
    expect(spectator.component).toBeTruthy();
  });

  it('renders style preview', () => {
    store.overrideSelector(selectSelectedDataSource, getDummySelectedDataSource({
      geometryType: AttributeTypeEnum.GEOMETRY,
    }));
    store.overrideSelector(selectStyles, styles);
    spectator.detectChanges();
    expect(spectator.queryAll('.polygon-item').length).toEqual(2);
    expect(spectator.queryAll('.line-item').length).toEqual(2);
    expect(spectator.queryAll('.point-item').length).toEqual(2);
    expect(spectator.query('rect').getAttribute('fill')).toEqual(styles[0].fillColor);
    expect(spectator.queryLast('line').getAttribute('stroke-width')).toEqual(`${styles[1].strokeWidth * 5}`);
    expect(spectator.queryLast<HTMLElement>('mat-icon').style.transform).toEqual(`scale(1.8)`);
  });

});

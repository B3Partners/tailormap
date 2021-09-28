import {
  Injector,
  NgModule,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AnalysisButtonComponent } from './analysis-button/analysis-button.component';
import { createCustomElement } from '@angular/elements';
import { SharedModule } from '../shared/shared.module';
import { StoreModule } from '@ngrx/store';
import { analysisStateKey } from './state/analysis.state';
import { analysisReducer } from './state/analysis.reducer';
import { CreateLayerPanelComponent } from './create-layer-panel/create-layer-panel.component';
import { CreateLayerFormComponent } from './create-layer-form/create-layer-form.component';
import { CreateLayerStylingComponent } from './create-layer-styling/create-layer-styling.component';
import { CreateLayerLayerSelectionComponent } from './create-layer-layer-selection/create-layer-layer-selection.component';
import { ApplicationModule } from '../application/application.module';
import { CriteriaComponent } from './criteria/criteria/criteria.component';
import { SimpleCriteriaComponent } from './criteria/simple-criteria/simple-criteria.component';
import { AdvancedCriteriaComponent } from './criteria/advanced-criteria/advanced-criteria.component';
import { CriteriaGroupComponent } from './criteria/criteria-group/criteria-group.component';
import { CriteriaDescriptionComponent } from './criteria/criteria-description/criteria-description.component';
import { CreateLayerModeAttributesComponent } from './create-layer-mode-attributes/create-layer-mode-attributes.component';
import { CreateLayerModeThematicComponent } from './create-layer-mode-thematic/create-layer-mode-thematic.component';
import { AttributeSelectorComponent } from './attribute-selector/attribute-selector.component';
import { StyleFormComponent } from './style-form/style-form.component';
import { EffectsModule } from '@ngrx/effects';
import { AnalysisEffects } from './state/analysis.effects';
import { StyleFormPanelComponent } from './style-form-panel/style-form-panel.component';
import { ResolutionSelectorComponent } from './resolution-selector/resolution-selector.component';
import { StylePreviewComponent } from './style-preview/style-preview.component';
import { ResolutionRangeSelectorComponent } from './resolution-range-selector/resolution-range-selector.component';
import { AnalysisAttributeListButtonComponent } from './analysis-attribute-list-button/analysis-attribute-list-button.component';
import { AttributeListService, CoreComponentsSharedModule } from '@tailormap/core-components';
import { AnalysisAttributeListLayerNameChooserComponent } from './analysis-attribute-list-layername-chooser/analysis-attribute-list-layer-name-chooser.component';


@NgModule({
  declarations: [
    AnalysisButtonComponent,
    CreateLayerPanelComponent,
    CreateLayerFormComponent,
    CreateLayerStylingComponent,
    CreateLayerLayerSelectionComponent,
    CriteriaComponent,
    SimpleCriteriaComponent,
    AdvancedCriteriaComponent,
    CriteriaGroupComponent,
    CriteriaDescriptionComponent,
    CreateLayerModeAttributesComponent,
    CreateLayerModeThematicComponent,
    AttributeSelectorComponent,
    StyleFormComponent,
    StyleFormPanelComponent,
    ResolutionSelectorComponent,
    StylePreviewComponent,
    ResolutionRangeSelectorComponent,
    AnalysisAttributeListButtonComponent,
    AnalysisAttributeListLayerNameChooserComponent,
  ],
  imports: [
    CommonModule,
    SharedModule,
    ApplicationModule,
    StoreModule.forFeature(analysisStateKey, analysisReducer),
    EffectsModule.forFeature([ AnalysisEffects ]),
    CoreComponentsSharedModule,
  ],
})
export class AnalysisModule {
  public constructor(
    injector: Injector,
    attributeListService: AttributeListService,
  ) {
    customElements.define('tailormap-analysis-button',
      createCustomElement(AnalysisButtonComponent, {injector}));
    customElements.define('tailormap-create-layer-panel',
      createCustomElement(CreateLayerPanelComponent, {injector}));
    AnalysisAttributeListButtonComponent.registerWithAttributeList(attributeListService);
  }
}

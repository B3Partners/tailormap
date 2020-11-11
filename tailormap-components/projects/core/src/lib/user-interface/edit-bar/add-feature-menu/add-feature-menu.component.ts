import {
  Component,
  NgZone,
  OnInit,
} from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Layer } from '../../attributelist/layer.model';
import { LayerUtils } from '../../../shared/layer-utils/layer-utils.service';
import { TailorMapService } from '../../../../../../bridge/src/tailor-map.service';
import { FormconfigRepositoryService } from '../../../shared/formconfig-repository/formconfig-repository.service';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { MatSelectChange } from '@angular/material/select';
import { FormConfiguration } from '../../../feature-form/form/form-models';

@Component({
  selector: 'tailormap-add-feature-menu',
  templateUrl: './add-feature-menu.component.html',
  styleUrls: ['./add-feature-menu.component.css'],
})
export class AddFeatureMenuComponent implements OnInit {

  public layer = 'Buurten';
  public layers: string[];

  private selectedConfig: FormConfiguration;

  constructor(
    private matIconRegistry: MatIconRegistry,
    private domSanitizer: DomSanitizer,
    public dialogRef: MatDialogRef<AddFeatureMenuComponent>,
    public tailorMapService: TailorMapService,
    private ngZone: NgZone,
    public formConfigRepo: FormconfigRepositoryService,
  ) {
    this.tailorMapService.layerVisibilityChanged$.subscribe(value => {
      this.ngZone.run(() => {
        this.calculateVisibleLayers();
      });
    });
    this.init();
  }

  public init(): void {
    this.formConfigRepo.formConfigs$.subscribe(formConfigs => {
      this.calculateVisibleLayers();
    });
  }

  public layerSelected(event: MatSelectChange): void {
    const layer: string = event.value;
    this.selectedConfig = this.formConfigRepo.getFormConfig(layer);
  }

  public calculateVisibleLayers(): void {
    this.layers = [];
    const allowFts = this.formConfigRepo.getFeatureTypes();

    const appLayers = this.tailorMapService.getViewerController().getVisibleLayers() as number[];
    appLayers.forEach(appLayerId => {
      const appLayer = this.tailorMapService.getViewerController().getAppLayerById(appLayerId);
      let layerName: string = appLayer.layerName;
      layerName = LayerUtils.sanitzeLayername(layerName);

      if (allowFts.findIndex(l => l.toLowerCase() === layerName) !== -1) {
        this.layers.push(layerName);
      }
    });
  }

  public ngOnInit(): void {
  }

  public close(): void {
    this.dialogRef.close();
  }

  public draw(type: string): void {
    alert('draw: ' + type);
  }

  public isPolygon(): boolean {
    const geomtype = this.selectedConfig?.featuretypeMetadata.geometryType;
    return geomtype === 'GEOMETRY' || geomtype === 'POLYGON';
  }

  public isLineString(): boolean {
    const geomtype = this.selectedConfig?.featuretypeMetadata.geometryType;
    return geomtype === 'GEOMETRY' || geomtype === 'LINESTRING';
  }

  public isPoint(): boolean {
    const geomtype = this.selectedConfig?.featuretypeMetadata.geometryType;
    return geomtype === 'GEOMETRY' || geomtype === 'POINT';
  }
}

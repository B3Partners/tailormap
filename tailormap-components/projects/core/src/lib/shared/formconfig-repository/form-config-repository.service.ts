import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormConfiguration, FormConfigurations } from '../../feature-form/form/form-models';
import { TailorMapService } from '../../../../../bridge/src/tailor-map.service';
import { FeatureControllerService, FeaturetypeMetadata } from '../generated';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, concatMap, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class FormConfigRepositoryService {

  constructor(
    private http: HttpClient,
    private featureController: FeatureControllerService,
    private tailorMap: TailorMapService,
  ) {}

  public loadFormConfiguration$(): Observable<Map<string, FormConfiguration>> {
    return this.http.get<FormConfigurations>(this.tailorMap.getContextPath() + '/action/form', {
      params: new HttpParams().set('application', '' + this.tailorMap.getApplicationId()),
    })
      .pipe(
        catchError((): Observable<FormConfigurations> => of({ config: {} })),
        concatMap((data: FormConfigurations) => {
          const formConfigs = new Map<string, FormConfiguration>();
          const featureTypes = [];
          for (const key in data.config) {
            if (data.config.hasOwnProperty(key)) {
              formConfigs.set(key, data.config[key]);
              featureTypes.push(key);
            }
          }
          return forkJoin([
            of(formConfigs),
            // @TODO: we need to check this... this loads feature type info from an application but with feature types from the form configs which are global
            this.featureController.featuretypeInformation({appId: this.tailorMap.getApplicationId(),featureTypes})
              .pipe(catchError((): Observable<Array<FeaturetypeMetadata>> => of([]))),
          ]);
        }),
        map(([ formConfigs, featureTypeInfo ]) => {
          if (!featureTypeInfo) {
            return new Map();
          }
          featureTypeInfo.forEach(featuretypeMetadata => {
            const layerName = featuretypeMetadata.featuretypeName;
            if (formConfigs.has(layerName)) {
              const config = formConfigs.get(layerName);
              formConfigs.set(layerName, {...config, featuretypeMetadata});
            }
          });
          return formConfigs;
        }),
      );
  }

}

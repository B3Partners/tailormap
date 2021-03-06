import { selectFeatureFormEnabled } from './form.selectors';
import { pipe } from 'rxjs';
import { select } from '@ngrx/store';
import { filter } from 'rxjs/operators';
import { Feature } from '../../shared/generated';
import { FeatureInitializerService } from '../../shared/feature-initializer/feature-initializer.service';

export const selectFormClosed = pipe(
  select(selectFeatureFormEnabled),
  filter(open => !open),
);

export const removeFeature = (features: Feature[], removed: Feature): Feature[] => {
  return features
    .filter(feature => feature.fid !== removed.fid)
    .map(feature => ({
      ...feature,
      children: feature.children ? removeFeature(feature.children, removed) : null,
    }));
};

export const updateFeatureInArray = (features: Feature[], newFeature: Feature): Feature[] => {
  const idx = features.findIndex(feature =>
    feature.fid === newFeature.fid || feature.fid === FeatureInitializerService.STUB_OBJECT_GUID_NEW_OBJECT);

  return (idx !== -1 ?
      [...features.slice(0, idx), {...newFeature}, ...features.slice(idx + 1)]
      : features
  )
    .map(feature => ({
      ...feature,
      children: feature.children ? updateFeatureInArray(feature.children, newFeature) : null,
    }));
};

export const addFeatureToParent = (features: Feature[], newFeature: Feature, parentId: string): Feature[] => {
  const idx = features.findIndex(feature => feature.fid === parentId);

  return (idx !== -1 ?
      [...features.slice(0, idx), {...features[idx], children: [...features[idx].children, newFeature]}, ...features.slice(idx + 1)]
      : features
  )
    .map(feature => ({
      ...feature,
      children: feature.children ? addFeatureToParent(feature.children, newFeature, parentId) : null,
    }));
};

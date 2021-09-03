export interface AttributeListParameters {
  application: number;
  appLayer: number;
  featureType?: number;
  layer?: number;  // only needed when featureType is not present
  limit?: number;  // default 10
  page?: number; // default 1
  start?: number;  // default 1
  dir?: string; // ASC/DESC (case sensitive)
  sort?: string; // name of property to be sorted
  // true (default) to get indexed results (c0:value,c1:value,... array), false to get normal response (attr_name:attr_value,...)
  arrays?: boolean;
  edit?: boolean;  // true for editing, false for not editing
  clearTotalCountCache?: boolean;
  filter?: string; // cql filter
  aliases?: boolean; // true (default) get attributes with their aliases instead of names
  includeRelations?: boolean; // true (default) to get filter for related features
  debug?: boolean; // true (default) to get non-gzipped response from WFS services
}

export interface AttributeListResponse {
  features: AttributeListFeature[];
  success: boolean;
  total: number;
  message?: string;
}

export interface AttributeListFeature {
  [key: string]: any;

  related_featuretypes?: RelatedFeatureType[];
  __fid: number | string;
}

export interface RelatedFeatureType {
  filter: string;
  foreignFeatureTypeName: string;
  id: number; // featureTypeId
}


export interface AttributeMetadataParameters {
  application: number;
  appLayer: number;
  featureType?: number;
}

export interface AttributeMetadataResponse {
  attributes: Attribute[];
  success: boolean;
  featureType: number;
  featureTypeName: string;
  geometryAttribute: string;
  primaryKeyAttribute: string;
  geometryAttributeIndex: number;
  relations: Relation[];
  invertedRelations: Relation[];
}

export interface Attribute {
  allowValueListOnly: boolean;
  automaticValue: boolean;
  defaultValue: string;
  disableUserEdit: boolean;
  disallowNullValue: boolean;
  editAlias: string;
  editHeight: string;
  editable: boolean;
  editValues?: string[];
  featureType: number;
  filterable: boolean;
  folder_label: string;
  id: number;
  longname: string;
  name: string;
  selectable: boolean;
  type: string;
  valueList: string;
  visible: boolean;
}

export interface Relation {
  featureType: number;
  foreignFeatureType: number;
  featureTypePrimaryKeyAttribute: string;
  type: RelationType;
  featureTypeName: string;
  foreignFeatureTypeName: string;
  foreignFeatureTypePrimaryKeyAttribute: string;
  relationKeys: RelationKey[];
  relations?: Relation[];
}

export interface RelationKey{
  leftSideName?: string;
  leftSideType?: string;
  rightSideName?: string;
  rightSideType?: string;
}

export enum RelationType {
  RELATE = 'relate',
  JOIN = 'join',
}

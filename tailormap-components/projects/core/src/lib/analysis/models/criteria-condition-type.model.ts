import { AttributeTypeEnum } from './attribute-type.enum';

export interface CriteriaConditionTypeModel {
  value: string;
  label: string;
  attributeType: AttributeTypeEnum;
  readable_label: string;
}

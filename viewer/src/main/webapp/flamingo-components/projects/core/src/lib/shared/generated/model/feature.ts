import {Geometry} from "./geometry";

/**
 * GBI Form REST API
 * \"REST API for GBI Forms\"
 *
 * OpenAPI spec version: 1.0.0
 * Contact: meinetoonen@b3partners.nl
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

export interface Feature {
    children?: Array<Feature>;
    object_guid?: string;
    objecttype: string;
    clazz?: string;
    geometrie?: Geometry;
}

import { GeoJSONGeometry } from 'wellknown';
import { Geometry } from '../../shared/generated';
import { NoOpWorkflow } from './NoOpWorkflow';
import { Coordinate } from '../../user-interface/models';

export class WorkflowHelpers {

  public static findTopRight(geojson: GeoJSONGeometry | Geometry): Coordinate {
    if (this.isGeneratedGeometry(geojson)) {
      return this.findUpperRightCoordinate(geojson.coordinates[0] as number[][]);
    } else {
      switch (geojson.type) {
        case 'MultiPolygon':
        case 'Polygon':
          return this.findUpperRightCoordinate(geojson.coordinates[0] as number[][])
        case 'LineString':
        case 'MultiLineString':
          return this.findUpperRightCoordinate(geojson.coordinates as number[][])
        case 'MultiPoint':
        case 'Point':
          return {
            x: geojson.coordinates[0] as number,
            y: geojson.coordinates[1] as number,
          };
        default:
          return {x: 0, y: 0};
      }
    }
  }

  private static findUpperRightCoordinate(coords: number[][]): Coordinate {
    let maxX = 0;
    let maxY = 0;
    coords.forEach(coordToCheck => {
      if (coordToCheck[0] > maxX && coordToCheck[1] > maxY) {
        maxX = coordToCheck[0];
        maxY = coordToCheck[1];
      }
    });
    return {
      x: maxX,
      y: maxY,
    };
  }

  public static isGeneratedGeometry(geom: any): geom is Geometry {
    return geom.crs;
  }
}

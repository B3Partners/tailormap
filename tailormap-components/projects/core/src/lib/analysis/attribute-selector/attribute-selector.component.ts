import { Component, Input, OnDestroy, EventEmitter, OnInit, Output, Inject } from '@angular/core';
import { BehaviorSubject, combineLatest, Observable, Subject } from 'rxjs';
import { map, startWith, takeUntil } from 'rxjs/operators';
import { MetadataService } from '../../application/services/metadata.service';
import { FormControl } from '@angular/forms';
import { AttributeTypeHelper } from '../../application/helpers/attribute-type.helper';
import { AttributeTypeEnum } from '../../shared/models/attribute-type.enum';
import { ExtendedAttributeModel } from '../../application/models/extended-attribute.model';
import { METADATA_SERVICE } from '@tailormap/api';

@Component({
  selector: 'tailormap-attribute-selector',
  templateUrl: './attribute-selector.component.html',
  styleUrls: ['./attribute-selector.component.css'],
})
export class AttributeSelectorComponent implements OnInit, OnDestroy {

  @Input()
  public set appLayerId(appLayerId: string | number) {
    if (this._appLayerId !== `${appLayerId}`) {
      this._appLayerId = `${appLayerId}`;
      this.getAppLayerMetadata();
    }
  }

  @Input()
  public set featureType(featureType: string | number) {
    if (this._featureType !== featureType) {
      this.filterAttributesForFeatureType(+(featureType));
      this.attributeControl.patchValue('');
    }
    this._featureType = +(featureType);
  }

  @Input()
  public set selectedAttribute(attribute: string) {
    if (this.attributeControl.value !== attribute) {
      this.attributeControl.patchValue(attribute);
      this._selectedAttribute = attribute;
    }
  }

  @Output()
  public attributeSelected = new EventEmitter<{ attribute: ExtendedAttributeModel; attributeType: AttributeTypeEnum }>();

  public filteredAttributes$: Observable<ExtendedAttributeModel[]>;

  private _featureType: number;
  private _selectedAttribute: string;
  private _appLayerId: string;

  private allAttributes: ExtendedAttributeModel[] = [];
  private allAttributesLookup: Map<string, ExtendedAttributeModel>;
  private availableAttributesSubject$ = new BehaviorSubject<ExtendedAttributeModel[]>([]);
  private destroyed = new Subject();

  public attributeControl = new FormControl('');

  public getPassportAlias = (attribute: string) => {
    if (!this.allAttributesLookup) {
      return '';
    }
    const att = this.allAttributesLookup.get(attribute);
    if (!att) {
      return attribute;
    }
    return att.alias;
  };

  constructor(
    @Inject(METADATA_SERVICE) private metadataService: MetadataService,
  ) { }

  public ngOnInit(): void {
    this.getAppLayerMetadata();

    this.filteredAttributes$ = combineLatest([
      this.availableAttributesSubject$.asObservable(),
      this.attributeControl.valueChanges.pipe(startWith('')),
    ]).pipe(
      takeUntil(this.destroyed),
      map(([ availableAttributes, value ]) => {
        const filterValue = value.toLowerCase();
        return availableAttributes.filter(attribute => attribute.name.toLowerCase().indexOf(filterValue) !== -1);
      }),
    );

    this.attributeControl.valueChanges.pipe(takeUntil(this.destroyed))
      .subscribe(value => this.attributeValueChanged(value));
  }

  public ngOnDestroy() {
    this.destroyed.next();
    this.destroyed.complete();
  }

  private getAppLayerMetadata() {
    if (!this._appLayerId) {
      return;
    }
    this.metadataService.getVisibleExtendedAttributesForLayer$(this._appLayerId).pipe(takeUntil(this.destroyed))
      .subscribe(attributes => {
        if (!attributes) {
          return;
        }
        this.allAttributes = attributes;
        this.allAttributesLookup = new Map<string, ExtendedAttributeModel>(attributes.map(a => [ a.name, a ]));
        if (this._featureType) {
          this.filterAttributesForFeatureType(this._featureType);
        }
      });
  }

  private filterAttributesForFeatureType(featureType: number) {
    const filteredAttributes = this.allAttributes.filter(attribute => {
      return attribute.featureType === +(featureType)
        && typeof AttributeTypeHelper.getAdministrativeAttributeType(attribute) !== 'undefined';
    });
    this.availableAttributesSubject$.next(filteredAttributes);

    if (this._selectedAttribute) {
      setTimeout(() => this.attributeControl.patchValue(this._selectedAttribute), 0);
    }
  }

  private attributeValueChanged(value: string) {
    const availableAttributes = this.availableAttributesSubject$.getValue();
    const attribute = availableAttributes.find(a => a.name === value);
    const attributeType = AttributeTypeHelper.getAdministrativeAttributeType(attribute);
    if (attribute && attributeType) {
      this.attributeSelected.emit({ attribute, attributeType });
    }
  }

}

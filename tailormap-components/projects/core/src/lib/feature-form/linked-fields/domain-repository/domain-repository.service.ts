import { Injectable, OnDestroy } from '@angular/core';
import { FormConfiguration, FormFieldType, SelectOption } from '../../form/form-models';
import { LinkedAttributeRegistryService } from '../registry/linked-attribute-registry.service';
import { AttributeControllerService, Attribuut } from '../../../shared/generated';
import { Observable, of, Subject } from 'rxjs';
import { map, takeUntil, tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class DomainRepositoryService implements OnDestroy {

  private destroyed = new Subject();

  constructor(
    private repo: AttributeControllerService,
    private registry: LinkedAttributeRegistryService) {
  }

  public ngOnDestroy(): void {
    this.destroyed.next();
    this.destroyed.complete();
  }

  public initFormConfig$(formConfigs: Map<string, FormConfiguration>): Observable<Map<string, FormConfiguration>> {
    const domainAttrs: Array<number> = [];
    formConfigs.forEach((config) => {
      config.fields.forEach(attribute => {
        if (attribute.type === FormFieldType.DOMAIN) {
          domainAttrs.push(attribute.linkedList);
        }
      });
    });
    if (domainAttrs.length === 0) {
      return of(formConfigs);
    }
    return this.repo.attributes({ids: domainAttrs})
      .pipe(
        takeUntil(this.destroyed),
        tap(linkedAttributes => {
          this.registry.setLinkedAttributes(linkedAttributes);
        }),
        map(result => {
          const linkedAttributes: Array<Attribuut> = result;
          for (const attribute of linkedAttributes) {
            const featureType = attribute.tabel_naam.toLowerCase();
            const fc: FormConfiguration = formConfigs.get(featureType);
            if (!fc) {
              continue;
            }
            fc.fields.forEach(field => {
              if (field.linkedList && field.linkedList === attribute.id) {
                const options: SelectOption[] = [];
                const domeinwaardes = attribute.domein.waardes;
                for (const domeinwaarde of domeinwaardes) {
                  options.push({
                    label: domeinwaarde.synoniem || domeinwaarde.waarde,
                    val: domeinwaarde.waarde,
                    disabled: false,
                    id: domeinwaarde.id,
                  });
                }
                options.sort((opt1, opt2) => opt1.label === opt2.label ? 0 : (opt1.label > opt2.label ? 1 : -1));
                field.options = options;
              }
            });
          }
          return formConfigs;
        }),
      );
  }

}

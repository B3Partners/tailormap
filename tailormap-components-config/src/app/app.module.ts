import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { ConfigModule } from "../config/config.module";
import { CONFIG_SERVICE_BASE_PATH, ConfigModule as CoreConfigModule } from "@tailormap/config";
import { ConfigModule as GbiMapsConfigModule } from "@b3partners/gbi-maps-components-config";
import { environment } from "../environments/environment";
import { HttpClientModule } from "@angular/common/http";
import { SharedModule } from "@tailormap/shared";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    SharedModule,
    CoreConfigModule,
    ConfigModule,
    GbiMapsConfigModule,
  ],
  providers: [
    { provide: CONFIG_SERVICE_BASE_PATH, useValue: environment.contextPath }
  ],
})
export class AppModule {
  public ngDoBootstrap(){}
}

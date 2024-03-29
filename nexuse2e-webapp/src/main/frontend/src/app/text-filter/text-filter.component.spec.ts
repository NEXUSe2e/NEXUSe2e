import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TextFilterComponent } from "./text-filter.component";
import { TranslateModule } from "@ngx-translate/core";
import { MatInputModule } from "@angular/material/input";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { FormsModule } from "@angular/forms";
import { By } from "@angular/platform-browser";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatIconModule } from "@angular/material/icon";
import { ActiveFilterList } from "../types";

describe("TextFilterComponent", () => {
  let component: TextFilterComponent;
  let fixture: ComponentFixture<TextFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TextFilterComponent],
      imports: [
        TranslateModule.forRoot(),
        MatInputModule,
        FormsModule,
        NoopAnimationsModule,
        MatFormFieldModule,
        MatAutocompleteModule,
        MatIconModule,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TextFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should display the field name", () => {
    component.fieldName = "Test";
    fixture.detectChanges();
    const label = fixture.nativeElement.querySelector("label");

    expect(label.innerText).toContain(component.fieldName);
  });

  it("should have a text input field", () => {
    const textField = fixture.nativeElement.querySelector("input[type='text']");

    expect(textField).toBeTruthy();
  });

  it("should emit the active filter as it is if no allowed values are set", async () => {
    spyOn(component.valueChange, "emit");
    component.allowedValues = [];
    const input = fixture.debugElement.query(By.css("input"));
    const test = "testValue";
    component.currentValue = test;
    input.triggerEventHandler("blur", {});
    fixture.detectChanges();
    const activeFilter: ActiveFilterList = {};
    activeFilter[component.fieldName] = test;

    expect(component.valueChange.emit).toHaveBeenCalledWith(activeFilter);
  });

  it("should empty the text field if allowed values are set and the value is not allowed", () => {
    spyOn(component.valueChange, "emit");
    component.allowedValues = ["one", "two", "three"];
    const input = fixture.debugElement.query(By.css("input"));
    component.selectedValue = "notAllowed";
    input.triggerEventHandler("blur", {});
    fixture.detectChanges();
    const activeFilter: ActiveFilterList = {};
    activeFilter[component.fieldName] = undefined;

    expect(component.valueChange.emit).toHaveBeenCalledWith(activeFilter);
  });
});

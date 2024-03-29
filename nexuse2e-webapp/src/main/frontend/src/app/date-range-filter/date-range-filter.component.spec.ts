import { ComponentFixture, TestBed } from "@angular/core/testing";

import { DateRangeFilterComponent } from "./date-range-filter.component";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatInputModule } from "@angular/material/input";
import { TranslateModule } from "@ngx-translate/core";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { FormsModule } from "@angular/forms";
import { By } from "@angular/platform-browser";
import { MatNativeDateModule } from "@angular/material/core";
import { MatIconModule } from "@angular/material/icon";
import { MatFormFieldModule } from "@angular/material/form-field";
import { ActiveFilterList } from "../types";

describe("DateRangeFilterComponent", () => {
  let component: DateRangeFilterComponent;
  let fixture: ComponentFixture<DateRangeFilterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DateRangeFilterComponent],
      imports: [
        TranslateModule.forRoot(),
        MatDatepickerModule,
        MatNativeDateModule,
        MatInputModule,
        BrowserAnimationsModule,
        FormsModule,
        MatIconModule,
        MatFormFieldModule,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DateRangeFilterComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should emit date when changed", () => {
    component.fieldName = "startEndDateRange";
    const startTest = new Date(new Date().setHours(3, 0, 0, 0));
    const activeFilter: ActiveFilterList = {};
    activeFilter["startEndDateRange"] = {
      startDate: startTest,
      endDate: component.endDate,
    };
    const inputStart = fixture.debugElement.query(
      By.css('input[name="startDate"]')
    );

    spyOn(component.valueChange, "emit");
    inputStart.triggerEventHandler("ngModelChange", startTest);
    fixture.detectChanges();

    expect(component.valueChange.emit).toHaveBeenCalledWith(activeFilter);
  });

  it("should emit undefined as value if both dates are undefined", () => {
    component.fieldName = "startEndDateRange";
    const activeFilter: ActiveFilterList = {};
    activeFilter["startEndDateRange"] = undefined;

    spyOn(component.valueChange, "emit");
    component.clearStartDatePicker();
    component.clearEndDatePicker();
    fixture.detectChanges();

    expect(component.valueChange.emit).toHaveBeenCalledWith(activeFilter);
  });

  it("should make the start date input be cleared", () => {
    component.startDate = new Date();
    component.clearStartDatePicker();

    expect(component.startDate).toBeUndefined();
  });

  it("should make the end date input be cleared", () => {
    component.endDate = new Date();
    component.clearEndDatePicker();

    expect(component.endDate).toBeUndefined();
  });
});

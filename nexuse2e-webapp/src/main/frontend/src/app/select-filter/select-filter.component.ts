import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { MatSelectChange } from "@angular/material/select";
import { ActiveFilterList } from "../types";

@Component({
  selector: "app-select-filter",
  templateUrl: "./select-filter.component.html",
  styleUrls: ["./select-filter.component.scss"],
})
export class SelectFilterComponent implements OnInit {
  @Input() fieldName!: string;
  @Input() allowedValues!: string[];
  @Input() selectedValue?: string;
  @Output() valueChange: EventEmitter<ActiveFilterList> = new EventEmitter();

  constructor() {}

  ngOnInit(): void {}

  emitValue(event: MatSelectChange) {
    const filters: ActiveFilterList = {};
    filters[this.fieldName] = event.value;
    this.valueChange.emit(filters);
  }
}

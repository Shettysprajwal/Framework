export interface TranslateCommand {
  regulationShortName: string;
  articleNumber: string;
  clauseNumber: string;
  rawSourceText: string;
}

export interface TranslationDetails {
  id: string;
  regulationShortName: string;
  articleNumber: string;
  clauseNumber: string;
  rawSourceText: string;
  deonticOperator: string;
  subject: string;
  action: string;
  target: string;
  constraint: string;
  smtSpec: string;
  odrlPolicy: string;
  isValid: boolean;
  validationMessage: string;
}

export interface ResolveQuery {
  subjectId: string;
  resourceId: string;
  actionId: string;
  sourceCountry: string;
  targetCountry: string;
}

export interface AttributeDetail {
  category: 'SUBJECT' | 'RESOURCE' | 'ACTION' | 'ENVIRONMENT';
  key: string;
  value: string;
  dataType: string;
}

export interface ResolvedContext {
  subjectId: string;
  resourceId: string;
  actionId: string;
  attributes: AttributeDetail[];
  transitiveAdequate: boolean;
}

export interface RegisterAttribute {
  id: string;
  key: string;
  value: string;
  dataType: string;
}

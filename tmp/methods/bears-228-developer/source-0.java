    public void handleSubmission(FormEntrySession session, HttpServletRequest submission) {
        if (dateWidget != null) {
            Date date = (Date) dateWidget.getValue(session.getContext(), submission);
            if (session.getSubmissionActions().getCurrentEncounter().getEncounterDatetime() != null
                    && !session.getSubmissionActions().getCurrentEncounter().getEncounterDatetime().equals(date)) {
                session.getContext().setPreviousEncounterDate(
                        new Date(session.getSubmissionActions().getCurrentEncounter().getEncounterDatetime().getTime()));
            }
            session.getSubmissionActions().getCurrentEncounter().setEncounterDatetime(date);
        }
        if (timeWidget != null) {
            Date time = (Date) timeWidget.getValue(session.getContext(), submission);
            Encounter e = session.getSubmissionActions().getCurrentEncounter();
            Date dateAndTime = HtmlFormEntryUtil.combineDateAndTime(e.getEncounterDatetime(), time);
            e.setEncounterDatetime(dateAndTime);
        }
        if (providerWidget != null) {
            Object value = providerWidget.getValue(session.getContext(), submission);
            if (value != null) {
                Person person = (Person) convertValueToProvider(value);
                EncounterCompatibility.setProvider(session.getSubmissionActions().getCurrentEncounter(), person);
            }
        }
        if (locationWidget != null) {
            Object value = locationWidget.getValue(session.getContext(), submission);
            if (value != null) {
                Location location = (Location) HtmlFormEntryUtil.convertToType(value.toString().trim(), Location.class);
                session.getSubmissionActions().getCurrentEncounter().setLocation(location);
            }
        }
        if (encounterTypeWidget != null) {
            EncounterType encounterType = (EncounterType) encounterTypeWidget.getValue(session.getContext(), submission);
            session.getSubmissionActions().getCurrentEncounter().setEncounterType(encounterType);
        }
        if (voidWidget != null) {
            if ("true".equals(voidWidget.getValue(session.getContext(), submission))) {
                session.setVoidEncounter(true);
            } else if ("false".equals(voidWidget.getValue(session.getContext(), submission))) {
                //nothing..  the session.voidEncounter property will be false, and the encounter will be un-voided if already voided
                //otherwise, nothing will happen.  99% of the time the encounter won't be voided to begin with.
            }
        }
    }

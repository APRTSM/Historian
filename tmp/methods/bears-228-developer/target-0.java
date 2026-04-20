    public void handleSubmission(FormEntrySession session, HttpServletRequest submission) {
        if (dateWidget != null) {
            Date date = (Date) dateWidget.getValue(session.getContext(), submission);
            Date previousDate = session.getSubmissionActions().getCurrentEncounter().getEncounterDatetime();

            if (previousDate == null) {
                session.getSubmissionActions().getCurrentEncounter().setEncounterDatetime(date);
            }

            else {
                // we don't want to lose any time information just because we edited it with a form that only collects date,
                // so we only update the date if the date has a time component or the actual date has changed
                if (hasTimeComponent(date) || !stripTimeComponent(date).equals(stripTimeComponent(previousDate))) {
                    session.getContext().setPreviousEncounterDate(
                            new Date(session.getSubmissionActions().getCurrentEncounter().getEncounterDatetime().getTime()));
                    session.getSubmissionActions().getCurrentEncounter().setEncounterDatetime(date);
                }
            }
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
    private boolean hasTimeComponent(Date date) {
        return !(new DateMidnight(date).toDate().equals(date));
    }
    private DateMidnight stripTimeComponent(Date date) {
        return new DateMidnight(date);
    }
